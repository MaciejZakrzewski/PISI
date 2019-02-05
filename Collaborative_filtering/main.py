import csv
import random
import copy
import math

K = 5
EPOCHS = 50
LAMBDA = 0.001
POSSIBLE_VALUES = [0, 1, 2, 3, 4, 5]


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


def convert_task_map_to_list(task_map, task_list):
    result = []

    for r in task_list:
        result.append((r[0], r[1], r[2], next(x[1] for x in task_map[r[1]] if x[0] == r[2])))

    return result


def generate_matrix(ids, counter):
    result_matrix = dict()

    for id_tmp in ids:
        result_matrix[id_tmp] = []
        for j in range(0, counter):
            result_matrix[id_tmp].append(random.uniform(-1.0, 1.0))

    return result_matrix


def count_error(output, expected):
    return 0.5 * pow((output - int(expected)), 2)


def gen_output(p, x):
    result = 0.0
    for i in range(0, len(x)):
        result += (p[i] * x[i])

    result += p[len(p) - 1]

    return int(round(result))


def create_sets_holder(data):
    result = dict()
    random.shuffle(data)
    result['training'] = data[0:int(0.9 * len(data))]
    result['validation'] = data[int(0.9 * len(data)):len(data)]
    return result


def compute_sum(derivs, fac, idx):
    sum = 0.0
    for key, value in derivs.items():
        sum += value * fac[key][idx]

    return sum


def find_hyper(errors):
    distances = dict()
    for i in range(0, len(errors)):
        err = errors[i]
        t = err[0]
        v = err[1]
        distance = math.sqrt(pow(t, 2) + pow(v, 2))
        distances[i] = distance

    return sorted(distances.items(), key=lambda kv: kv[1])[0][0]


def train(training_map, errors, movies_ids, full_train, features_matrices, p_factor_list):
    people_ids = set()

    for key in training_map.keys():
        people_ids.add(key)

    for i in range(1, 11):
        features_matrix = generate_matrix(movies_ids, i)
        p_factors = generate_matrix(people_ids, i + 1)

        validation_errors = []
        train_errors = []

        for j in range(0, EPOCHS * i):
            holder = create_sets_holder(full_train)

            derivative = dict()

            for person_id in training_map.keys():
                derivative[person_id] = dict()

                for person in training_map[person_id]:
                    derivative[person_id][person[0]] = 0.0

            train_err = []

            for p in holder['training']:
                output = gen_output(p_factors[p[1]], features_matrix[p[2]])
                train_err.append(count_error(output, p[3]))
                actual_deriv = derivative[p[1]][p[2]]
                new_deriv = actual_deriv + (output - float(p[3]))

                derivative[p[1]][p[2]] = new_deriv

            train_errors.append(sum(train_err) / float(len(train_err)))
            saved_features = copy.deepcopy(features_matrix)
            for key, value in saved_features.items():
                deriv_for_movie = dict()
                for der_key, der_val in derivative.items():
                    if key in der_val:
                        deriv_for_movie[der_key] = der_val[key]

                new_values = []
                for k in range(0, len(value)):
                    new_value = value[k] - LAMBDA * compute_sum(deriv_for_movie, p_factors, k)
                    new_values.append(new_value)
                saved_features[key] = new_values

            for key, value in p_factors.items():
                derivs_for_person = derivative[key]
                new_values = []
                for k in range(0, len(value) - 1):
                    new_value = value[k] - LAMBDA * compute_sum(derivs_for_person, saved_features, k)
                    new_values.append(new_value)
                new_values.append(value[len(value) - 1] - LAMBDA * sum(derivs_for_person.values()))
                p_factors[key] = new_values

            validation_err = []
            for v in holder['validation']:
                output = gen_output(p_factors[v[1]], features_matrix[v[2]])
                validation_err.append(count_error(output, v[3]))

            validation_errors.append(sum(validation_err) / float(len(validation_err)))

        errors.append([sum(validation_errors) / float(len(validation_errors)),
                       sum(train_errors) / float(len(train_errors))])

        features_matrices.append(features_matrix)
        p_factor_list.append(p_factors)


def main():
    training = read_csv('train.csv')

    movies = read_csv('movie.csv')

    task = read_csv('task.csv')

    training_map = prepare_map(training)

    task_map = prepare_map(task)

    movies_ids = set()

    for movie in movies:
        movies_ids.add(movie[0])

    errors = []

    features_matrices = []

    p_factor_list = []

    train(training_map, errors, movies_ids, training, features_matrices, p_factor_list)

    hyper = find_hyper(errors)

    features_matrix = features_matrices[hyper]
    p_factors = p_factor_list[hyper]

    for person in task_map:
        result = []
        p = task_map[person]

        for tupl in p:
            prediction = gen_output(p_factors[person], features_matrix[tupl[0]])
            if prediction not in POSSIBLE_VALUES:
                prediction = random.randint(0, 5)
            result.append((tupl[0], str(prediction)))

        task_map[person] = result

    to_save = convert_task_map_to_list(task_map, task)
    write_csv('submission.csv', to_save)
    print('DONE')


if __name__ == '__main__':
    main()
