// Author: Harper Davis

use std::{marker::PhantomData, rc::{Rc}};

use crate::{state::State, action::Action, frontier::{QueueFrontier, Frontier, StackFrontier, PriorityQueueFrontier}, node::Node};

#[derive(Debug)]
pub struct Solution<S: State<Action = A>, A: Action> {
    start_state: S,
    final_state: S,
    path: Vec<A>,
}

///////////////////////////////////////////////////////

impl<S: State<Action = A>, A: Action> Solution<S, A> {

    pub fn new(start_state: S, final_state: S, path: Vec<A>) -> Solution<S, A> {
        Solution { start_state, final_state, path }
    }

    pub fn display(&self) {
        println!("Solution Found!");
        self.display_final();
        println!("Path ({}): ", self.path_length());
        self.display_path();
    }

    pub fn display_final(&self) {
        println!("Start: ");
        self.start_state.display_pretty();
        println!("Final: ");
        self.final_state.display_pretty();
    }

    pub fn display_path(&self) {
        for action in &self.path {
            print!("{} ", action.to_string());
        }
        println!()
    }

    pub fn path_length(&self) -> u32 {
        self.path.len() as u32
    }

    pub fn get_path(&self) -> &Vec<A> {
        &self.path
    }

}

pub trait Search<S: State<Action = A>, A: Action, F: Frontier<S, A>>: ToString {

    fn new(limit: f64) -> Self;

    fn search(&mut self, start_state: S) -> Option<Solution<S, A>> {
        let mut frontier = F::new();
        frontier.insert(Node::new(&start_state, 0.0, 0));

        while !frontier.is_empty() {
            let node = frontier.pop().unwrap();

            if node.get_state().is_goal_state() {
                let mut path = Vec::new();
                let mut current_node = &node;
                while current_node.get_parent_node().is_some() {
                    path.insert(0, current_node.get_action().unwrap().to_owned());
                    current_node = current_node.get_parent_node().unwrap();
                }
                return Some(Solution::new(start_state.clone(), node.get_state().clone(), path));
            }

            let rc_node = Rc::new(node);
            for action in rc_node.get_possible_actions() {
                let parent_node = rc_node.get_parent_node();
                let next_node = Node::<S, A>::next_node(rc_node.clone(), &action);

                if (parent_node.is_some() && parent_node.unwrap().get_state() == next_node.get_state()) || self.prune(&next_node) {
                    continue;
                }

                frontier.insert(next_node);
            }
        }

        return None;
    }

    fn prune(&mut self, node: &Node<S, A>) -> bool;
}

pub struct TreeSearch<S: State<Action = A>, A: Action, F: Frontier<S, A>> {
    phantom_s: PhantomData<S>,
    phantom_a: PhantomData<A>,
    phantom_f: PhantomData<F>,
}

impl <S: State<Action = A>, A: Action, F: Frontier<S, A>> ToString for TreeSearch<S, A, F> {
    fn to_string(&self) -> String {
        "TreeSearch".to_string()
    }
}

impl<S: State<Action = A>, A: Action, F: Frontier<S, A>> Search<S, A, F> for TreeSearch<S, A, F> {

    fn new(_limit: f64) -> TreeSearch<S, A, F> {
        TreeSearch { phantom_a: PhantomData, phantom_s: PhantomData, phantom_f: PhantomData }
    }

    fn prune(&mut self, _node: &Node<S, A>) -> bool {
        false
    }

}
///////////////////////////////////////////////////////
/// 

pub struct DepthLimitedSearch<S: State<Action = A>, A: Action, F: Frontier<S, A>> {
    depth_limit: u32,
    phantom_s: PhantomData<S>,
    phantom_a: PhantomData<A>,
    phantom_f: PhantomData<F>,
}

impl <S: State<Action = A>, A: Action, F: Frontier<S, A>> ToString for DepthLimitedSearch<S, A, F> {
    fn to_string(&self) -> String {
        "DepthLimitedSearch".to_string()
    }
}

impl<S: State<Action = A>, A: Action, F: Frontier<S, A>> Search<S, A, F> for DepthLimitedSearch<S, A, F> {

    fn new(limit: f64) -> DepthLimitedSearch<S, A, F> {
        DepthLimitedSearch { depth_limit: limit as u32, phantom_a: PhantomData, phantom_s: PhantomData, phantom_f: PhantomData }
    }

    fn prune(&mut self, node: &Node<S, A>) -> bool {
        node.get_depth() > self.depth_limit
    }

}

pub type BreadthFirstSearch<S, A> = TreeSearch<S, A, QueueFrontier<S, A>>;
pub type DepthFirstSearch<S, A> = TreeSearch<S, A, StackFrontier<S, A>>;

///////////////////////////////////////////////////////

pub struct IterativeDeepeningSearch<S: State<Action = A>, A: Action, F: Frontier<S, A>> {
    phantom_s: PhantomData<S>,
    phantom_a: PhantomData<A>,
    phantom_f: PhantomData<F>,
}

impl<S: State<Action = A>, A: Action, F: Frontier<S, A>> ToString for IterativeDeepeningSearch<S, A, F> {
    fn to_string(&self) -> String {
        "IterativeDeepeningSearch".to_string()
    }
}

impl<S: State<Action = A>, A: Action, F: Frontier<S, A>> Search<S, A, F> for IterativeDeepeningSearch<S, A, F> {

    fn new(_limit: f64) -> IterativeDeepeningSearch<S, A, F> {
        IterativeDeepeningSearch { phantom_a: PhantomData, phantom_s: PhantomData, phantom_f: PhantomData }
    }

    fn search(&mut self, start_state: S) -> Option<Solution<S, A>> {
        let mut depth = 1.0;
        loop {
            let mut search = DepthLimitedSearch::<S, A, F>::new(depth);
            let solution = search.search(start_state.clone());
            if solution.is_some() {
                return solution;
            }
            depth += 1.0;
        }
    }

    fn prune(&mut self, _node: &Node<S, A>) -> bool {
        unreachable!()
    }

}

pub type IterativeDeepeningDepthFirstSearch<S, A> = IterativeDeepeningSearch<S, A, StackFrontier<S, A>>;
pub type AStarSearch<S, A> = DepthLimitedSearch<S, A, PriorityQueueFrontier<S, A>>;

pub struct EvaluationLimitedSearch<S: State<Action = A>, A: Action, F: Frontier<S, A>> {
    evaluation_limit: f64,
    lowest_evaluation_above_limit: f64,
    phantom_s: PhantomData<S>,
    phantom_a: PhantomData<A>,
    phantom_f: PhantomData<F>,
}

impl<S: State<Action = A>, A: Action, F: Frontier<S, A>> EvaluationLimitedSearch<S, A, F> {

    fn get_lowest_evaluation_above_limit(&self) -> f64 {
        self.lowest_evaluation_above_limit
    }
}

impl<S: State<Action = A>, A: Action, F: Frontier<S, A>> ToString for EvaluationLimitedSearch<S, A, F> {
    
    fn to_string(&self) -> String {
        "HeuristicLimitedSearch".to_string()
    }

}

impl<S: State<Action = A>, A: Action, F: Frontier<S, A>> Search<S, A, F> for EvaluationLimitedSearch<S, A, F> {

    fn new(limit: f64) -> EvaluationLimitedSearch<S, A, F> {
        EvaluationLimitedSearch { evaluation_limit: limit, lowest_evaluation_above_limit: f64::INFINITY, phantom_s: PhantomData, phantom_a: PhantomData, phantom_f: PhantomData }
    }

    fn prune(&mut self, node: &Node<S, A>) -> bool {
        let eval = node.eval();
        if eval > self.evaluation_limit {
            self.lowest_evaluation_above_limit = f64::min(self.lowest_evaluation_above_limit, eval);
            true
        } else {
            false
        }
    }

}

pub struct IterativeDeepeningEvaluationSearch<S: State<Action = A>, A: Action, F: Frontier<S, A>> {
    phantom_s: PhantomData<S>,
    phantom_a: PhantomData<A>,
    phantom_f: PhantomData<F>,
}

impl<S: State<Action = A>, A: Action, F: Frontier<S, A>> ToString for IterativeDeepeningEvaluationSearch<S, A, F> {
    
    fn to_string(&self) -> String {
        "IterativeDeepeningEvaluationSearch".to_string()
    }

}

impl<S: State<Action = A>, A: Action, F: Frontier<S, A>> Search<S, A, F> for IterativeDeepeningEvaluationSearch<S, A, F> {

    fn new(_limit: f64) -> IterativeDeepeningEvaluationSearch<S, A, F> {
        IterativeDeepeningEvaluationSearch { phantom_s: PhantomData, phantom_a: PhantomData, phantom_f: PhantomData }
    }

    fn search(&mut self, start_state: S) -> Option<Solution<S, A>> {
        let mut evaluation_limit = start_state.heuristic();
        loop {
            let mut search = EvaluationLimitedSearch::<S, A, F>::new(evaluation_limit);
            let solution = search.search(start_state.clone());
            if solution.is_some() {
                return solution;
            } else {
                evaluation_limit = search.get_lowest_evaluation_above_limit();
            }
        }
    }

    fn prune(&mut self, _node: &Node<S, A>) -> bool {
        unreachable!()
    }

}

pub type IterativeDeepeningAStarSearch<S, A> = IterativeDeepeningEvaluationSearch<S, A, StackFrontier<S, A>>;