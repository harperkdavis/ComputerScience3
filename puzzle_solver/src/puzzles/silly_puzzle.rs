// Author: Harper Davis
use std::str::FromStr;

use colored::Colorize;

use crate::{state::State, action::Action};

#[derive(PartialEq, Eq, Clone, Hash)]
pub struct SillyPuzzle {
    n: i32,
}

impl SillyPuzzle {
    pub fn new(n: i32) -> SillyPuzzle {
        SillyPuzzle { n }
    }
}

impl ToString for SillyPuzzle {
    
    fn to_string(&self) -> String {
        self.n.to_string()
    }
    
}

impl State for SillyPuzzle {
    type Action = SillyPuzzleAction;

    fn display_pretty(&self) {
        let mut s = ".".repeat(100);
        s.replace_range(self.n as usize..(self.n+1) as usize, "o");
        s.replace_range(69..70, "*");
        
        println!("{:^102}\n[{}]", "T H E   S I L L Y   P U Z Z L E".magenta(), s.cyan())
    }

    fn list_actions(&self) -> Vec<Self::Action> {
        vec![SillyPuzzleAction::new(1), SillyPuzzleAction::new(-1)]
    }

    fn perform_action(&self, action: &Self::Action) -> Self {
        SillyPuzzle::new(self.n + action.change)
    }

    fn is_goal_state(&self) -> bool {
        self.n == 69
    }

    fn heuristic(&self) -> f64 {
        f64::abs((69 - self.n).into())
    }

}

#[derive(PartialEq, Clone)]
pub struct SillyPuzzleAction {
    change: i32,
}

impl SillyPuzzleAction {
    fn new(change: i32) -> SillyPuzzleAction {
        SillyPuzzleAction { change }
    }
}

impl ToString for SillyPuzzleAction {
    
    fn to_string(&self) -> String {
        if self.change == 1 {
            "+".to_string()
        } else {
            "-".to_string()
        }
    }
}

impl FromStr for SillyPuzzleAction {

    type Err = ();

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        if s == "+" {
            Ok(SillyPuzzleAction::new(1))
        } else if s == "-" {
            Ok(SillyPuzzleAction::new(-1))
        } else {
            Err(())
        }
    }

}

impl Action for SillyPuzzleAction {

    fn get_cost(&self) -> f64 {
        1.0
    }

}