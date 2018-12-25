import tmdbsimple as tmdb
import csv

tmdb.API_KEY = '852eabf1faf621f417234193b0ca6f95'

if __name__ == '__main__':
    movie = tmdb.Movies(2)
    response = movie.info()

    print(movie.title)