import csv

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


def prepare_map_with_dict(list_to_refactor):
    person_id_list = dict()

    for row in list_to_refactor:
        if row[1] not in person_id_list:
            person_id_list[row[1]] = dict()

    for row in list_to_refactor:
        x = person_id_list[row[1]]
        x[row[2]] = row[3]
        person_id_list[row[1]] = x

    return person_id_list


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


def get_sorted_best_match_list(person, training_map):
    best_match = []
    for person_to_compare in training_map:
        result = 0
        if person != person_to_compare:
            val_of_person = training_map[person]
            val_to_compare = training_map[person_to_compare]

            for tup in val_of_person:
                try:
                    tup_to_compare = val_to_compare[tup]
                except KeyError:
                    tup_to_compare = None

                if tup_to_compare is None:
                    result += 5
                else:
                    result += abs(int(tup_to_compare) - int(val_of_person[tup]))

            best_match.append((person_to_compare, result))
    return sorted(best_match, key=lambda tup_to_sort: tup_to_sort[1])


def main():
    training = read_csv('train.csv')

    task = read_csv('task.csv')

    training_map = prepare_map_with_dict(training)
    task_map = prepare_map_with_dict(task)

    task_map_to_save = prepare_map(task)

    for person in task_map:
        result = []
        best_match = get_sorted_best_match_list(person, training_map)
        list_to_evaluate = task_map[person]

        for tupl in list_to_evaluate:
            tupl_sum = 0
            counter = 0
            for best in best_match:
                try:
                    to_add = training_map[best[0]][tupl]
                except KeyError:
                    to_add = None
                if to_add is not None:
                    tupl_sum += int(to_add)
                    counter += 1

                if counter == K:
                    break

            result.append((tupl, str(int(round(tupl_sum / K)))))

        task_map_to_save[person] = result

    to_save = convert_task_map_to_list(task_map_to_save, task)
    write_csv('submission.csv', to_save)
    print('DONE')


if __name__ == '__main__':
    main()


