use std::io::{self, Write};

use rand::Rng;
use colored::*;

use crate::data;

pub fn sigmoid(x: f64) -> f64 {
    1.0 / (1.0 + f64::exp(-x))
}

pub fn error_signal(answer: f64, correct: f64) -> f64 {
    (correct - answer) * answer * (1.0 - answer)
}

#[derive(Debug, Clone)]
pub struct Layer {
    size: u32,
    output_size: u32,
    weights: Vec<f64>,
}

impl Layer {

    pub fn new_random(size: u32, output_size: u32) -> Layer {

        let total_weights = (size + 1) * output_size;
        let mut random = rand::thread_rng();

        let mut weights: Vec<f64> = Vec::new();
        for _ in 0..total_weights {
            let random_weight: f64 = random.gen();
            weights.push((random_weight - 0.5) * 0.1);
        }

        Layer::new(size, output_size, weights)
    }

    pub fn new(size: u32, output_size: u32, weights: Vec<f64>) -> Layer {
        Layer {
            size,
            output_size,
            weights
        }
    }

    pub fn process(&self, values: &Vec<f64>) -> Option<Vec<f64>> {

        if values.len() != self.size.try_into().unwrap() {
            println!("Wrong size!");
            return None
        }

        let mut output: Vec<f64> = Vec::new();
        for o in 0..self.output_size {

            let mut sum = 0.0;
            for i in 0..(self.size + 1) {

                let value = if i == 0 { 1.0 } else { values[(i - 1) as usize] };
                let weight_index = i * self.output_size + o;
                let weight = self.weights[weight_index as usize];

                sum += value * weight;

            }

            output.push(sigmoid(sum));
        }

        Some(output)
    }

    pub fn pretty_print_layer(&self) {
        print!("──────────┤ ");
        for i in 0..self.output_size {
            print!("Out {: >3} │ ", i + 1);
        }
        print!("\n");
        io::stdout().flush().unwrap();
        

        for i in 0..(self.size + 1) {
            if i == 0 {
                print!("{}{}", "Bias     ", " │ ");
            } else {
                print!("Input {: >3} │ ", i);
            }

            for j in 0..self.output_size {
                let index = i * self.output_size + j;
                let weight = self.weights[index as usize];
                let formatted = format!("{0:>7.4}", weight).on_truecolor(
                    f64::max(f64::sqrt(weight) * 256.0, 0.0) as u8, 
                    0, 
                    f64::max(f64::sqrt(-weight) * 256.0, 0.0) as u8
                );
                print!("{}   ", formatted)
            }

            print!("\n");
            io::stdout().flush().unwrap();
        }
        
    }

}
pub struct Network {
    layers: Vec<Layer>
}

impl Network { 

    pub fn new_random(layer_sizes: Vec<u32>) -> Network {
        let mut layers: Vec<Layer> = Vec::new();

        for i in 1..layer_sizes.len() {
            let prev_size = layer_sizes[i - 1];
            let size = layer_sizes[i];
            layers.push(Layer::new_random(prev_size, size));
        }

        Network {
            layers
        }
    }

    pub fn load_from_file(path: &str) -> Result<Network, Box<dyn std::error::Error>> {
        let mut reader = csv::ReaderBuilder::new().flexible(true).from_path(path)?;

        let headers = reader.headers()?;
        println!("{:?}", headers);

        let mut layer_sizes: Vec<u32> = Vec::new();

        for header in headers {
            let size: u32 = header.to_string().parse()?;
            layer_sizes.push(size);
        }

        let mut layers: Vec<Layer> = Vec::new();

        let mut i = 0;
        for result in reader.records() {
            let weights_data = result?;
            let mut weights: Vec<f64> = Vec::new();

            for weight in &weights_data {
                let value: f64 = weight.to_string().parse()?;
                weights.push(value);
            }

            let new_layer = Layer {
                size: layer_sizes[i],
                output_size: layer_sizes[i + 1],
                weights
            };
            layers.push(new_layer);

            i += 1;
        }

        Ok(Network {
            layers
        })
    }

    pub fn save_to_file(&self, path: &str) -> Result<(), Box<dyn std::error::Error>> {
        let mut writer = csv::WriterBuilder::new().flexible(true).from_path(path)?;

        let mut layer_sizes: Vec<String> = vec![self.layers[0].size.to_string()];
        for layer in &self.layers {
            layer_sizes.push(layer.output_size.to_string());
        }

        writer.write_record(&layer_sizes)?;

        for layer in &self.layers {
            let mut str_weights: Vec<String> = vec![];
            for weight in &layer.weights {
                str_weights.push(weight.to_string());
            }
            writer.write_record(&str_weights)?;
        }
        
        writer.flush()?;

        Ok(())
    }

    pub fn forward(&self, input: Vec<f64>) -> Option<Vec<Vec<f64>>> {
        let mut layer_output = vec![input];

        for layer in &self.layers {
            let next_input = layer.process(layer_output.last().unwrap());
            layer_output.push(next_input.unwrap());
        }

        Some(layer_output)
    }

    pub fn train_one(&mut self, data_point: &DataPoint, learning_rate: f64) -> bool {
        let correct_output = &data_point.output;
        let input = &data_point.input;

        let output = self.forward(input.to_vec()).unwrap();

        let mut all_error_signals: Vec<Vec<f64>> = Vec::new();

        let mut i = 0;
        for layer_outputs in output.iter().rev() {
            if i >= output.len() - 1 {
                break;
            }

            let comparing_vector = if i == 0 { &correct_output } else { all_error_signals.last().unwrap() };
            
            let mut layer_error_signals: Vec<f64> = Vec::new();
            if i == 0 {
                for j in 0..layer_outputs.len() {
                    layer_error_signals.push(error_signal(layer_outputs[j], comparing_vector[j]));
                }
            } else {

                let layer = &self.layers[self.layers.len() - i];
                for j in 0..layer.size {
                    
                    let mut sum = 0.0;

                    for k in 0..layer.output_size {
                        let weight_index = (j + 1) * layer.output_size + k;
                        sum += comparing_vector[k as usize] * layer.weights[weight_index as usize];
                    }

                    let output = layer_outputs[j as usize];
                    layer_error_signals.push(sum * (output) * (1.0 - output));
                }

            }

            all_error_signals.push(layer_error_signals);
            i += 1;
        }

        let result = Self::was_correct(output.last().unwrap(), correct_output);

        let mut i = 0;

        for layer in self.layers.iter_mut().rev() {
            let error_signals = &all_error_signals[i];

            for j in 0..(layer.size + 1) {

                let result = if j == 0 { 1.0 } else { output[output.len() - i - 2][(j - 1) as usize] };
                for k in 0..layer.output_size {
                    let error_signal = error_signals[k as usize];
                    let weight_index = (j * layer.output_size + k) as usize;
                    let old_weight = layer.weights[weight_index];
                    
                    layer.weights[weight_index] = old_weight + error_signal * result * learning_rate;
                }
                
            }
            i += 1;
        }

        result
    }

    pub fn train_epoch(&mut self, data_set: &DataSet, learning_rate: f64) {
        let mut i = 0;
        let mut sum = 0;
        for data_point in &data_set.data {
            let result = self.train_one(data_point, learning_rate);

            if result {
                sum += 1;
            }
            i += 1;

            if data_set.data.len() > 5000 {
                println!("[TRAINING] {}/{} {} (Accuracy: {})", i, data_set.data.len(), if result { "[RIGHT]".green() } else { "[WRONG]".red() }, sum as f64 / i as f64);
            }
        }
    }

    pub fn train(&mut self, training_set: &DataSet, validation_set: &DataSet, testing_set: &DataSet, accuracy_threshold: f64, learning_rate: f64) {
        println!("Beginning training...");

        let mut accuracy = 0.0;
        let mut i = 0;
        while accuracy < accuracy_threshold {
            self.train_epoch(training_set, learning_rate);
            
            let mut sum = 0;
            let mut count = 0;
            for point in &validation_set.data {
                let result = Network::was_correct(self.forward(point.input.to_vec()).unwrap().last().unwrap(), &point.output);
                if result {
                    sum += 1;
                }

                count += 1;
                println!("[VALIDATING] {}/{} {}, (Accuracy: {})", count, validation_set.data.len(), if result { "[RIGHT]".green() } else { "[WRONG]".red() }, sum as f64 / count as f64);
            }
            accuracy = sum as f64 / count as f64;
            println!("Epoch {i} completed. Accuracy: {accuracy} ({sum}/{count})");
            i += 1;
        }

        println!("Training complete. Took {i} epochs.");

        let mut sum = 0;
        let mut count = 0;
        for point in &testing_set.data {
            let result = Network::was_correct(self.forward(point.input.to_vec()).unwrap().last().unwrap(), &point.output);

            if result {
                sum += 1;
            }

            count += 1;
            println!("[TESTING] {}/{} {}, (Accuracy: {})", count, testing_set.data.len(), if result { "[RIGHT]".green() } else { "[WRONG]".red() }, sum as f64 / count as f64);
        }

        accuracy = sum as f64 / count as f64;
        println!("Final testing accuracy: {accuracy} ({sum}/{count})");
    }

    pub fn was_correct(actual_output: &Vec<f64>, correct_output: &Vec<f64>) -> bool {

        let mut highest_actual = -1.0;
        let mut actual_index = 0;
        let mut highest_correct = -1.0;
        let mut correct_index = 0;

        for i in 0..correct_output.len() {
            if correct_output[i] > highest_correct {
                correct_index = i;
                highest_correct = correct_output[i];
            }
        }

        for i in 0..actual_output.len() {
            if actual_output[i] > highest_actual {
                actual_index = i;
                highest_actual = actual_output[i];
            }
        }

        actual_index == correct_index
    }

    pub fn pretty_print(&self) {
        for layer in &self.layers {
            layer.pretty_print_layer();
        }
    }

}

#[derive(Clone, Debug)]
pub struct DataSet {
    pub data: Vec<DataPoint>,
    pub input_size: u32,
    pub output_size: u32,
}

#[derive(Clone, Debug)]
pub struct DataPoint {
    pub input: Vec<f64>,
    pub output: Vec<f64>
}

impl DataSet {

    pub fn new(data: Vec<DataPoint>, input_size: u32, output_size: u32) -> DataSet {
        DataSet { data, input_size, output_size }
    }

    pub fn load_from_file(path: &str) -> Result<DataSet, Box<dyn std::error::Error>> {
        let mut reader = csv::ReaderBuilder::new().flexible(true).from_path(path)?;

        let headers = reader.headers()?;
        let input_size: u32 = headers.get(0).unwrap().to_string().parse()?;
        let output_size: u32 = headers.get(1).unwrap().to_string().parse()?;

        let mut data: Vec<DataPoint> = Vec::new();

        for record in reader.records() {
            let mut input: Vec<f64> = Vec::new();
            let mut output: Vec<f64> = Vec::new();
            
            let all_data = record?;
            let mut i = 0;

            for data in &all_data {

                let value = data.parse()?;
                if i < input_size {
                    input.push(value);
                } else {
                    output.push(value);
                }

                i += 1;
            }

            data.push(DataPoint { input, output });
        }

        Ok(Self::new(data, input_size, output_size))
    }

    pub fn load_from_file_mapped(path: &str, input_map: &dyn Fn(&Vec<f64>, &Vec<f64>) -> Vec<f64>, output_map: &dyn Fn(&Vec<f64>, &Vec<f64>) -> Vec<f64>) -> Result<DataSet, Box<dyn std::error::Error>> {
        let mut data_set = Self::load_from_file(path)?;

        for point in &mut data_set.data {
            let new_input = input_map(&point.input, &point.output);
            let new_output = output_map(&point.output, &point.input);

            point.input = new_input;
            point.output = new_output;
        }

        Ok(data_set)
    }

}