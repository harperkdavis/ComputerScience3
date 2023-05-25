// Harper Davis

use std::{fs, collections::{HashMap, BinaryHeap}, cmp::Ordering, time::SystemTime, thread::{self, JoinHandle}, sync::{Arc, Mutex}};
use indicatif::ProgressBar;

const RATINGS_PATH: &'static str = "data/ratings.txt";
const VALIDATION_PATH: &'static str = "data/validation.txt";
const TEST_PATH: &'static str = "data/test.txt";

const OUTPUT_PATH: &'static str = "output/validation-predictions.txt";

const K_VALUE: u32 = 27;
const THREADS: u32 = 20;

type UserId = u32;
type MovieId = u32;
type RatingValue = f64;

#[derive(Clone, Copy)]
struct Rating {
    movie_id: MovieId,
    user_id: UserId,
    rating_value: RatingValue,
}

#[derive(Clone, Copy)]
struct Test {
    movie_id: MovieId,
    user_id: UserId,
}

#[derive(Clone)]
struct MovieWithRatings {
    ratings_by_users: HashMap<UserId, RatingValue>
}

#[derive(Clone)]
struct UserWithRatings {
    ratings_of_movies: HashMap<MovieId, RatingValue>
}

#[derive(Debug)]
struct SimiliarRating {
    rating_value: RatingValue,
    similiarity: f64,
}

impl PartialEq for SimiliarRating {
    fn eq(&self, other: &Self) -> bool {
        self.similiarity == other.similiarity
    }
}

impl PartialOrd for SimiliarRating {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        other.similiarity.partial_cmp(&self.similiarity)
    }
}

impl Eq for SimiliarRating {}

impl Ord for SimiliarRating {
    fn cmp(&self, other: &Self) -> Ordering {
        self.partial_cmp(other).unwrap_or(Ordering::Equal)
    }
}

fn load_rating_from_string(string: String) -> Rating {
    let split_string: Vec<&str> = string.split(';').collect();
    Rating { 
        movie_id: split_string[0].to_string().parse().unwrap(), 
        user_id: split_string[1].to_string().parse().unwrap(), 
        rating_value: split_string[2].to_string().parse().unwrap() 
    }
}

fn predict_simple(
    movie_id: MovieId, 
    _user_id: UserId, 
    movies_with_ratings: &HashMap<MovieId, MovieWithRatings>, 
    _users_with_ratings: &HashMap<UserId, UserWithRatings>,
    _k: u32,
) -> RatingValue {
    let mut average = 0.0;
    let mut count = 0.0;

    for (_, rating_value) in &movies_with_ratings.get(&movie_id).unwrap().ratings_by_users {
        average += rating_value;
        count += 1.0;
    }

    average / count
}

fn predict_knn(
    movie_id: MovieId, 
    user_id: UserId, 
    movies_with_ratings: &HashMap<MovieId, MovieWithRatings>, 
    users_with_ratings: &HashMap<UserId, UserWithRatings>,
    k: u32,
) -> RatingValue {
    get_distance_simple(&get_closest_points(movie_id, user_id, movies_with_ratings, users_with_ratings), k)
}

fn get_distance_simple(points: &BinaryHeap<SimiliarRating>, k: u32) -> RatingValue {
    let mut average = 0.0;
    let mut count = 0.0;

    let mut i = 0;
    for similiar_rating in points {
        average += similiar_rating.rating_value;
        count += 1.0;

        i += 1;
        if i > k {
            break;
        }
    }

    average / count
}

// Creates a priority queue of the closest points so we can test k-values
fn get_closest_points(
    movie_id: MovieId, 
    user_id: UserId, 
    movies_with_ratings: &HashMap<MovieId, MovieWithRatings>, 
    users_with_ratings: &HashMap<UserId, UserWithRatings>,
) -> BinaryHeap<SimiliarRating> {
    let users_that_have_seen_movie = &movies_with_ratings.get(&movie_id).unwrap().ratings_by_users;
    let movies_that_user_has_seen = &users_with_ratings.get(&user_id).unwrap().ratings_of_movies;

    let mut most_similiar_ratings: BinaryHeap<SimiliarRating> = BinaryHeap::new();

    for user_tuple in users_that_have_seen_movie {

        let (other_user_id, other_user_rating_of_movie) = user_tuple;
        let other_user_ratings = &users_with_ratings.get(&other_user_id).unwrap().ratings_of_movies;

        let mut similiarity = 0.0;
        let mut count = 0.0;

        for movie_tuple in movies_that_user_has_seen {

            let (movie_id, user_rating) = movie_tuple;

            if other_user_ratings.contains_key(movie_id) {

                let ra = user_rating;
                let rb = other_user_ratings.get(movie_id).unwrap();

                similiarity += (ra - rb) * (ra - rb);
                count += 1.0;

            }
            
        }

        if count > 0.0 {
            most_similiar_ratings.push(SimiliarRating { 
                rating_value: *other_user_rating_of_movie, 
                similiarity: similiarity / count
            });
        }

    }

    most_similiar_ratings
}

fn calculate_rmse(data: Vec<(f64, f64)>) -> f64 {
    let mut rmse = 0.0;
    let mut count = 0.0;

    for point in data {
        let (a, b) = point;
        rmse += (a - b) * (a - b);
        count += 1.0;
    }

    f64::sqrt(rmse / count)
}

fn validate(
    validation_set: &Vec<Rating>, 
    prediction_function: &dyn Fn(MovieId, UserId, &HashMap<MovieId, MovieWithRatings>, &HashMap<UserId, UserWithRatings>, u32) -> RatingValue,
    movies_with_ratings: &HashMap<MovieId, MovieWithRatings>, 
    users_with_ratings: &HashMap<UserId, UserWithRatings>,
    k: u32
) -> f64 {

    let mut predictions: Vec<(RatingValue, RatingValue)> = Vec::new();
    let bar = ProgressBar::new(validation_set.len().try_into().unwrap());

    for point in validation_set {
        let actual_value = point.rating_value;
        let prediction_value = prediction_function(point.movie_id, point.user_id, &movies_with_ratings, &users_with_ratings, k);

        predictions.push((prediction_value, actual_value));
        bar.inc(1);
    }

    bar.finish();

    calculate_rmse(predictions)
}

fn validate_multithreaded(
    validation_set: &Vec<Rating>, 
    movies_with_ratings: &HashMap<MovieId, MovieWithRatings>, 
    users_with_ratings: &HashMap<UserId, UserWithRatings>,
    k: u32
) -> f64 {

    let mut predictions: Vec<(RatingValue, RatingValue)> = Vec::new();
    for _ in 0..validation_set.len() {
        predictions.push((0.0, 0.0));
    }

    let predictions_ref = Arc::new(Mutex::new(predictions));

    let movies_with_ratings_ref = Arc::new(movies_with_ratings.clone());
    let users_with_ratings_ref = Arc::new(users_with_ratings.clone());

    let mut handles: Vec<JoinHandle<()>> = Vec::new();

    let stride = validation_set.len() as f64 / THREADS as f64;
    let mut sum = 0.0;

    println!("Spawning threads...");
    for _ in 0..THREADS {

        let mut chunk: Vec<(u32, Rating)> = Vec::new();
        for j in sum as u32..(sum + stride) as u32 {
            chunk.push((j, validation_set[j as usize]));
        }

        let chunk_ref = Arc::new(chunk);

        let local_pred_ref = predictions_ref.clone();
        let local_mwr_ref = movies_with_ratings_ref.clone();
        let local_uwr_ref = users_with_ratings_ref.clone();

        // let bar_ref = bar.clone();
        let handle = thread::spawn(move || {
            let mut predictions: Vec<(u32, (RatingValue, RatingValue))> = Vec::new();
            for point in chunk_ref.iter() {
                
                let (index, rating) = point;
                let actual_value = rating.rating_value;
                let prediction_value = predict_knn(rating.movie_id, rating.user_id, &local_mwr_ref, &local_uwr_ref, k);
        
                predictions.push((*index, (prediction_value, actual_value)));
            }

            let mut pred_ref = local_pred_ref.lock().unwrap();
            for pred in predictions {
                let (index, tuple) = pred;

                pred_ref[index as usize] = tuple;
            }
        });

        handles.push(handle);

        sum += stride;
    }

    println!("Threads spawned, awaiting completion.");

    for handle in handles {
        handle.join().expect("Thread panicked!");
    }

    println!("Threads finished.");

    calculate_rmse(Arc::try_unwrap(predictions_ref).unwrap().into_inner().unwrap())
}

fn validate_knn_with_variable_k(
    validation_set: &Vec<Rating>, 
    movies_with_ratings: &HashMap<MovieId, MovieWithRatings>, 
    users_with_ratings: &HashMap<UserId, UserWithRatings>,
) {

    let mut data_set: Vec<(BinaryHeap<SimiliarRating>, RatingValue)> = Vec::new();
    let bar = ProgressBar::new(validation_set.len().try_into().unwrap());

    for point in validation_set {
        let closest = get_closest_points(point.movie_id, point.user_id, movies_with_ratings, users_with_ratings);
        data_set.push((closest, point.rating_value));
        bar.inc(1);
    }

    bar.finish();

    for k in 0..100 {

        let mut predictions: Vec<(RatingValue, RatingValue)> = Vec::new();

        for (points, actual_value) in &data_set {

            let prediction_value = get_distance_simple(points, k);
            predictions.push((prediction_value, *actual_value));

        }

        println!("RMSE for {}: {}", k, calculate_rmse(predictions));
    }

}

fn _test(
    test_set: &Vec<Test>, 
    prediction_function: &dyn Fn(MovieId, UserId, &HashMap<MovieId, MovieWithRatings>, &HashMap<UserId, UserWithRatings>, u32) -> RatingValue,
    movies_with_ratings: &HashMap<MovieId, MovieWithRatings>, 
    users_with_ratings: &HashMap<UserId, UserWithRatings>,
    k: u32
) -> Vec<RatingValue> {
    let mut predictions: Vec<RatingValue> = Vec::new();

    let bar = ProgressBar::new(test_set.len().try_into().unwrap());
    for point in test_set {
        let prediction_value = prediction_function(point.movie_id, point.user_id, &movies_with_ratings, &users_with_ratings, k);

        predictions.push(prediction_value);
        bar.inc(1);
    }
    bar.finish();

    predictions
}

fn test_multithreaded(
    test_set: &Vec<Test>, 
    movies_with_ratings: &HashMap<MovieId, MovieWithRatings>, 
    users_with_ratings: &HashMap<UserId, UserWithRatings>,
    k: u32
) -> Vec<RatingValue> {

    let mut predictions: Vec<RatingValue> = Vec::new();
    for _ in 0..test_set.len() {
        predictions.push(0.0);
    }

    let predictions_ref = Arc::new(Mutex::new(predictions));

    let movies_with_ratings_ref = Arc::new(movies_with_ratings.clone());
    let users_with_ratings_ref = Arc::new(users_with_ratings.clone());

    let mut handles: Vec<JoinHandle<()>> = Vec::new();

    let stride = test_set.len() as f64 / THREADS as f64;
    let mut sum = 0.0;

    println!("Spawning threads...");
    for _ in 0..THREADS {

        let mut chunk: Vec<(u32, Test)> = Vec::new();
        for j in sum as u32..(sum + stride) as u32 {
            chunk.push((j, test_set[j as usize]));
        }

        let chunk_ref = Arc::new(chunk);

        let local_pred_ref = predictions_ref.clone();
        let local_mwr_ref = movies_with_ratings_ref.clone();
        let local_uwr_ref = users_with_ratings_ref.clone();

        let handle = thread::spawn(move || {
            let mut predictions: Vec<(u32, RatingValue)> = Vec::new();
            for point in chunk_ref.iter() {
                
                let (index, rating) = point;
                let prediction_value = predict_knn(rating.movie_id, rating.user_id, &local_mwr_ref, &local_uwr_ref, k);
        
                predictions.push((*index, prediction_value));
            }

            let mut pred_ref = local_pred_ref.lock().unwrap();
            for pred in predictions {
                let (index, value) = pred;

                pred_ref[index as usize] = value;
            }
        });

        handles.push(handle);

        sum += stride;
    }
    println!("Threads spawned, awaiting completion.");

    for handle in handles {
        handle.join().expect("Thread panicked!");
    }

    println!("Threads finished.");

    Arc::try_unwrap(predictions_ref).unwrap().into_inner().unwrap()
}

fn main() {

    println!("Loading files...");

    let ratings_file = fs::read_to_string(RATINGS_PATH).unwrap();
    let validation_file = fs::read_to_string(VALIDATION_PATH).unwrap();

    let ratings_file_split = ratings_file.split('\n');
    let validation_file_split = validation_file.split('\n');
    
    let mut training_set: Vec<Rating> = Vec::new();
    let mut validation_set: Vec<Rating> = Vec::new();

    println!("Loading training dataset...");

    for rating_string in ratings_file_split {
        if rating_string.trim().len() <= 0 {
            continue;
        }
        let new_rating = load_rating_from_string(rating_string.to_string());
        training_set.push(new_rating);
    }
    
    println!("Loading validation dataset...");

    for rating_string in validation_file_split {
        if rating_string.trim().len() <= 0 {
            continue;
        }
        let new_rating = load_rating_from_string(rating_string.to_string());
        validation_set.push(new_rating);
    }

    let mut movies_with_ratings: HashMap<MovieId, MovieWithRatings> = HashMap::new();
    let mut users_with_ratings: HashMap<UserId, UserWithRatings> = HashMap::new();

    println!("Loading training data into data structures...");

    for training_point in training_set {

        let movie_id = training_point.movie_id;
        let user_id = training_point.user_id;
        let rating_value = training_point.rating_value;
        
        if movies_with_ratings.get(&movie_id).is_none() {
            let new_movie_with_ratings = MovieWithRatings {
                ratings_by_users: HashMap::new()
            };
            movies_with_ratings.insert(movie_id, new_movie_with_ratings);
        }

        movies_with_ratings.get_mut(&movie_id).as_mut().unwrap().ratings_by_users.insert(user_id, rating_value);

        if users_with_ratings.get(&user_id).is_none() {
            let new_user_with_ratings = UserWithRatings {
                ratings_of_movies: HashMap::new()
            };
            users_with_ratings.insert(user_id, new_user_with_ratings);
        }

        users_with_ratings.get_mut(&user_id).as_mut().unwrap().ratings_of_movies.insert(movie_id, rating_value);

    }

    println!("Running simple algorithm (bogus predictor)...");
    let simple_predictor_result = validate(&validation_set, &predict_simple, &movies_with_ratings, &users_with_ratings, 0);
    println!("Simple rmse: {}", simple_predictor_result);

    let now = SystemTime::now();
    println!("Running knn algorithm (single threaded) (k={K_VALUE})...");
    let knn_result = validate(&validation_set, &predict_knn, &movies_with_ratings, &users_with_ratings, K_VALUE);
    println!("Knn rmse: {}", knn_result);
    println!("Validation (single threaded) took {} ms.", now.elapsed().unwrap().as_millis());

    let now = SystemTime::now();
    println!("Running knn algorithm (multithreaded) (k={K_VALUE})...");
    let knn_result = validate_multithreaded(&validation_set, &movies_with_ratings, &users_with_ratings, K_VALUE);
    println!("Knn rmse: {}", knn_result);
    println!("Validation (multithreaded) took {} ms.", now.elapsed().unwrap().as_millis());

    println!("Running knn algorithm with variable k...");
    validate_knn_with_variable_k(&validation_set, &movies_with_ratings, &users_with_ratings);

    println!("Generating test data...");
    let test_file = fs::read_to_string(TEST_PATH).unwrap();

    let test_file_split = test_file.split('\n');

    let mut tests: Vec<Test> = Vec::new();

    for rating_string in test_file_split {
        
        if rating_string.trim().len() <= 0 {
            continue;
        }
        let split_string: Vec<&str> = rating_string.split(';').collect();

        tests.push(Test {
            movie_id: split_string[0].to_string().parse().unwrap(), 
            user_id: split_string[1].to_string().parse().unwrap(), 
        });
        
    }

    let now = SystemTime::now();
    println!("Running knn on test data (multithreaded) (k={K_VALUE})...");

    let test_data = test_multithreaded(&tests, &movies_with_ratings, &users_with_ratings, K_VALUE);
    let mut test_string: String = "".to_string();

    for point in test_data {
        test_string.push_str(&(point.to_string() + &"\n".to_string()));
    }

    fs::write(OUTPUT_PATH, test_string).expect("File write didn't work!");
    println!("Test completed in {} ms.", now.elapsed().unwrap().as_millis());
}