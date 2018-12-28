import tmdbsimple as tmdb
import csv
import redis
import pickle
import operator
import arrow

tmdb.API_KEY = '852eabf1faf621f417234193b0ca6f95'
K = 5


def read_csv(file_name):
    training = []

    with open(file_name, newline='') as train_file:
        reader = csv.reader(train_file, delimiter=';')
        for row in reader:
            training.append(row)

    return training


def write_csv(file_name, result):
    with open(file_name, newline='', mode='w') as task_file:
        writer = csv.writer(task_file, delimiter=';')
        for r in result:
            writer.writerow([r[0], r[1], r[2], r[3]])


def get_movie_info_from_api(movies_csv):
    movies = dict()
    for movie in movies_csv:
        if movie[0] not in movies:
            movies[movie[0]] = tmdb.Movies(movie[1]).info()

    return movies


def prepare_movies_map(movies):
    movies_dict = dict()

    for row in movies:
        if row[0] not in movies_dict:
            movies_dict[row[0]] = (row[1], row[2])

    return movies_dict


def prepare_map(list_to_refactor):
    person_id_list = dict()

    for row in list_to_refactor:
        if row[1] not in person_id_list:
            person_id_list[row[1]] = []

    for row in list_to_refactor:
        x = person_id_list[row[1]]
        x.append((row[2], row[3]))
        person_id_list[row[1]] = x

    return person_id_list


def evaluate_based_on_knn(movies_info, task_tuple, training_list):
    evaluated_movie = movies_info[task_tuple[0]]

    evaluated_movie_collection = evaluated_movie['belongs_to_collection']

    nearest_dict = dict()

    for tr_tup in training_list:
        if tr_tup[0] not in nearest_dict:
            nearest_dict[tr_tup[0]] = 0

        if evaluated_movie_collection is not None:
            if movies_info[tr_tup[0]]['belongs_to_collection'] is not None:
                if evaluated_movie_collection['id'] != movies_info[tr_tup[0]]['belongs_to_collection']['id']:
                    nearest_dict[tr_tup[0]] += 1
            else:
                nearest_dict[tr_tup[0]] += 1

        if (evaluated_movie['budget'] * 0.9 > movies_info[tr_tup[0]]['budget']) or (movies_info[tr_tup[0]]['budget'] > evaluated_movie['budget'] * 1.1):
            nearest_dict[tr_tup[0]] += 0.8

        for genre in evaluated_movie['genres']:
            if genre not in movies_info[tr_tup[0]]['genres']:
                nearest_dict[tr_tup[0]] += 1

        if (evaluated_movie['popularity'] * 0.85 > movies_info[tr_tup[0]]['popularity']) or (movies_info[tr_tup[0]]['popularity'] > evaluated_movie['popularity'] * 1.15):
            nearest_dict[tr_tup[0]] += 0.8

        for company in evaluated_movie['production_companies']:
            if company not in movies_info[tr_tup[0]]['production_companies']:
                nearest_dict[tr_tup[0]] += 1

        for country in evaluated_movie['production_countries']:
            if country not in movies_info[tr_tup[0]]['production_countries']:
                nearest_dict[tr_tup[0]] += 0.5

        if evaluated_movie['original_language'] != movies_info[tr_tup[0]]['original_language']:
            nearest_dict[tr_tup[0]] += 1

        if (evaluated_movie['vote_average'] - 0.5 > movies_info[tr_tup[0]]['vote_average']) or (movies_info[tr_tup[0]]['vote_average'] + 0.5 > evaluated_movie['vote_average']):
            nearest_dict[tr_tup[0]] += 0.8

        movie_date = arrow.get(evaluated_movie['release_date'], 'YYYY-MM-DD').date()
        compared_date = arrow.get(movies_info[tr_tup[0]]['release_date'], 'YYYY-MM-DD').date()

        if (movie_date.year - 10 > compared_date.year) or (compared_date.year + 10 > movie_date.year):
            nearest_dict[tr_tup[0]] += 0.8

    sorted_nearest_dict = sorted(nearest_dict.items(), key=operator.itemgetter(1))[:K]

    result = 0

    for s in sorted_nearest_dict:
        result += int(next(x for x in training_list if x[0] == s[0])[1])

    return str(int(round(result / K)))


def cache_movies(movies):
    r = redis.Redis(host='localhost', port=6379, db=0)

    if r.get('movies') is not None:
        return pickle.loads(r.get('movies'))
    else:
        movies_info = get_movie_info_from_api(movies)
        r.set('movies', pickle.dumps(movies_info))
        return movies_info


def convert_task_map_to_list(task_map, task_list):
    result = []

    for r in task_list:
        result.append((r[0], r[1], r[2], next(x[1] for x in task_map[r[1]] if x[0] == r[2])))

    return result


def main():
    training = read_csv('train.csv')

    movies = read_csv('movie.csv')

    task = read_csv('task.csv')

    training_map = prepare_map(training)

    task_map = prepare_map(task)

    movies_info = cache_movies(movies)

    for person in task_map:
        result = []
        p = task_map[person]
        tr = training_map[person]

        for tupl in p:
            result.append((tupl[0], evaluate_based_on_knn(movies_info, tupl, tr)))

        task_map[person] = result

    to_save = convert_task_map_to_list(task_map, task)
    write_csv('submission.csv', to_save)
    print('DONE')


if __name__ == '__main__':
    main()


