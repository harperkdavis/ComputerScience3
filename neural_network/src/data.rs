use std::fs;

use crate::model::{DataSet, DataPoint};

pub fn load_apalydin_kaynak_numbers(path: &str) -> Result<DataSet, Box<dyn std::error::Error>> {
    DataSet::load_from_file_mapped(path, 
        &|input, _| {
            let mut new_input: Vec<f64> = Vec::new();

            for value in input {
                new_input.push(*value / 16.0);
            }

            new_input
        },
        &|output, _| {
            let mut new_output: Vec<f64> = Vec::new();

            for i in 0..10 {
                new_output.push(if i == output[0] as u32 { 1.0 } else { 0.0 });
            }

            new_output
        }
    )
}

pub fn load_mnist(path: &str) -> Result<DataSet, Box<dyn std::error::Error>> {
    DataSet::load_from_file_mapped(path, 
        &|_, input| {
            let mut new_input: Vec<f64> = Vec::new();

            for value in input {
                new_input.push(*value / 256.0);
            }

            new_input
        },
        &|_, output| {
            let mut new_output: Vec<f64> = Vec::new();

            for i in 0..10 {
                new_output.push(if i == output[0] as u32 { 1.0 } else { 0.0 });
            }

            new_output
        }
    )
}