pub mod model;
pub mod data;

use model::*;
use data::*;

fn train_and() {
    let mut network = Network::new_random(vec![2, 2]);
    let data_set = DataSet::load_from_file("data/and.csv").unwrap();

    network.train(&data_set, &data_set, &data_set, 1.0, 0.2);
    network.pretty_print();
}

fn train_xor() {
    let mut network = Network::new_random(vec![2, 2, 2]);
    let data_set = DataSet::load_from_file("data/xor.csv").unwrap();

    network.train(&data_set, &data_set, &data_set, 1.0, 0.05);
    network.pretty_print();
}

fn train_apalydin_kaynak_numbers() {
    let mut network = Network::new_random(vec![64, 40, 10]);

    let train_set = load_apalydin_kaynak_numbers("data/numbers_train_raw.csv").unwrap();
    let test_set = load_apalydin_kaynak_numbers("data/numbers_test_raw.csv").unwrap();

    network.train(&train_set, &train_set, &test_set, 0.97, 0.1);
}

fn train_mnist() {
    let mut network = Network::new_random(vec![784, 200, 10]);

    println!("Loading data sets...");

    let train_set = load_mnist("data/mnist_train.csv").unwrap();
    let test_set = load_mnist("data/mnist_test.csv").unwrap();

    println!("{:?}", train_set.data[0]);
    println!("i: {}, o: {}", train_set.data[0].input.len(), train_set.data[0].output.len());

    network.train(&train_set, &train_set, &test_set, 0.90, 0.1);
    network.save_to_file("numbers.csv");
}

fn main() {
    
    // train_and();
    // train_xor();
    // train_apalydin_kaynak_numbers();
    // train_mnist();
}