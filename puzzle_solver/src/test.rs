// Author: Harper Davis
use std::{time::Instant, fs, collections::HashMap};

use colored::Colorize;

use crate::{search::{Search, Solution}, state::State, action::Action, puzzles::{fifteen_puzzle::{FifteenPuzzle, FifteenPuzzleAction}}, frontier::Frontier};

pub struct TestCase<S: State<Action = A>, A: Action> {
    start_state: S,
    correct_path: Vec<A>,
    limit: f64,
    depth_limit: u32,
}

impl<S: State<Action = A>, A: Action> TestCase<S, A> {
    pub fn new(start_state: S, correct_path: Vec<A>, limit: f64) -> TestCase<S, A> {
        TestCase { start_state, correct_path, limit, depth_limit: limit as u32 }
    }

    fn get_limit(&self) -> f64 {
        self.limit
    }

    fn get_depth_limit(&self) -> u32 {
        self.depth_limit
    }

    fn get_correct_path(&self) -> &Vec<A> {
        &self.correct_path
    }

    fn get_start_state(&self) -> &S {
        &self.start_state
    }

    fn check_solution(&self, solution: &Solution<S, A>) -> bool {
        let path = solution.get_path();
        if path.len() > self.correct_path.len() {
            println!("Failed because path lengths is greater: {} > {}", path.len(), self.correct_path.len());
            return false;
        }
        // for i in 0..path.len() {
        //     if path[i] != self.correct_path[i] {
        //         println!("Failed because path is different at index {}: {} vs {}", i, path[i].to_string(), self.correct_path[i].to_string());
        //         return false;
        //     }
        // }
        true
    }
}

pub fn timed_test<S: State<Action = A>, A: Action, F: Frontier<S, A>, E: Search<S, A, F>>(start_state: S, limit: f64) -> (Option<Solution<S, A>>, f64) {
    let mut searcher = E::new(limit);
    let start = Instant::now();
    let solution = searcher.search(start_state);
    let elapsed = (start.elapsed().as_micros() as f64) / 1000.0;
    (solution, elapsed)
}

pub fn test_one<S: State<Action = A>, A: Action, F: Frontier<S, A>, E: Search<S, A, F>>(test_case: &TestCase<S, A>) -> (bool, f64) {
    let (solution, time) = timed_test::<S, A, F, E>(test_case.get_start_state().clone(), test_case.get_limit());
    if solution.is_none() {
        println!("No solution found!");
        (false, time)
    } else {
        (test_case.check_solution(&solution.unwrap()), time)
    }
}

pub fn test<S: State<Action = A>, A: Action, F: Frontier<S, A>, E: Search<S, A, F>>(test_cases: Vec<TestCase<S, A>>) {
    let mut averages_for_depth: HashMap<u32, (f64, f64)> = HashMap::new();
    
    let mut prev_depth = if let Some(test_case) = test_cases.first() { test_case.depth_limit } else { 0 };
    for test_case in &test_cases {

        if test_case.depth_limit != prev_depth {
            let (num_tests, total_time) = averages_for_depth.get(&prev_depth).unwrap();
            println!("{} avg for depth {} {} ms", "[AVERAGE]".blue(), prev_depth, format!("{: >10.3}", (total_time / num_tests)).yellow());
        }
        prev_depth = test_case.depth_limit;

        let (result, time) = test_one::<S, A, F, E>(test_case);
        
        print!("\r");
        println!("{} with depth {}, took {: >10.3} ms", if result { "[PASS]".green() } else { "[FAIL]".red() }, test_case.depth_limit, time);

        averages_for_depth.entry(test_case.depth_limit).and_modify(|x| {
            x.0 += 1.0;
            x.1 += time;
        }).or_insert((1.0, time));

        
    }

    println!("{}", "[FINISHED]".yellow());
}

pub fn test_fifteen_puzzle_from_file<F: Frontier<FifteenPuzzle, FifteenPuzzleAction>, E: Search<FifteenPuzzle, FifteenPuzzleAction, F>>(test_cases_path: &'static str) {
    let mut test_cases = Vec::new();
    for line in fs::read_to_string(test_cases_path).unwrap().lines() {
        let split = line.split(" ").collect::<Vec<&str>>();
        let depth_limit = split[0].parse::<u32>().unwrap();
        let starting_state = u64::from_str_radix(split[3].trim_start_matches("0x"), 16).unwrap();
        let correct_path = split[5].split("-").map(|x| FifteenPuzzleAction::new(x.parse::<u8>().unwrap())).collect::<Vec<FifteenPuzzleAction>>();
        let start_state = FifteenPuzzle::new(starting_state);
        let test_case = TestCase::new(start_state, correct_path, depth_limit as f64);
        test_cases.push(test_case);
    }

    test::<FifteenPuzzle, FifteenPuzzleAction, F, E>(test_cases);
}