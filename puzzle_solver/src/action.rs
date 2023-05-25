// Author: Harper Davis
use std::str::FromStr;

pub trait Action: Clone + FromStr + ToString + PartialEq {

    fn get_cost(&self) -> f64;
    
}