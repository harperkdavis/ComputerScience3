// Author: Harper Davis

use frontier::{StackFrontier, PriorityQueueFrontier};
use puzzles::{silly_puzzle::{SillyPuzzle}, fifteen_puzzle::{FifteenPuzzle}, rubiks_cube::CubeState};
use search::{Search, DepthFirstSearch, AStarSearch, IterativeDeepeningAStarSearch};

use test::{test_fifteen_puzzle_from_file, TestCase, test};

use crate::puzzles::{fifteen_puzzle::FifteenPuzzleAction, rubiks_cube::CubeAction};


pub mod state;
pub mod action;
pub mod search;
pub mod node;
pub mod frontier;

pub mod puzzles;

pub mod test;


fn test_silly_puzzle() {
    let start_state = SillyPuzzle::new(0);
    let mut search = DepthFirstSearch::new(100.0);
    let solution = search.search(start_state);
    match solution {
        Some(solution) => solution.display(),
        None => println!("No Solution Found!"),
    }
}

fn test_fifteen_puzzle() {
    // ugh
    test_fifteen_puzzle_from_file::<PriorityQueueFrontier<FifteenPuzzle, FifteenPuzzleAction>, AStarSearch<FifteenPuzzle, FifteenPuzzleAction>>("./tests/15_puzzle.txt");
}

fn test_rubiks_cube() {
    let mut test_set = Vec::new();
    for i in 1..100 {
        for _ in 0..20 {
            let (state, actions) = CubeState::scrambled_cube(i);
            test_set.push(TestCase::new(state, actions, i as f64));
        }
    }
    test::<CubeState, CubeAction, PriorityQueueFrontier<CubeState, CubeAction>, AStarSearch<CubeState, CubeAction>>(test_set)
}

fn main() {
    // test_silly_puzzle();
    test_fifteen_puzzle();
    // test_rubiks_cube();

}
