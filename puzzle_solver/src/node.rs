// Author: Harper Davis

use std::rc::Rc;

use crate::{state::State, action::Action};

#[derive(Debug, Clone)]
pub struct Node<S: State<Action = A>, A: Action> {
    state: S,
    action: Option<A>,
    parent_node: Option<Rc<Self>>,
    path_cost: f64,
    depth: u32,
}

impl <'a, S: State<Action = A>, A: Action> PartialEq for Node<S, A> {
    fn eq(&self, other: &Self) -> bool {
        self.state == other.state
    }
}

impl <'a, S: State<Action = A>, A: Action> Eq for Node<S, A> {}

impl <'a, S: State<Action = A>, A: Action> PartialOrd for Node<S, A> {
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        Some(self.cmp(other))
    }
}

impl <'a, S: State<Action = A>, A: Action> Ord for Node<S, A> {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        self.eval().partial_cmp(&other.eval()).unwrap_or(std::cmp::Ordering::Equal).reverse()
    }
}

impl <'a, S: State<Action = A>, A: Action> Node<S, A> {
    pub fn new(state: &S, path_cost: f64, depth: u32) -> Node<S, A> {
        Node { state: state.clone(), parent_node: None, action: None, path_cost, depth }
    }

    pub fn get_state(&self) -> &S {
        &self.state
    }

    pub fn get_action(&self) -> Option<&A> {
        self.action.as_ref()
    }

    pub fn get_parent_node(&self) -> Option<&Node<S, A>> {
        self.parent_node.as_deref()
    }

    pub fn get_possible_actions(&self) -> Vec<A> {
        self.state.list_actions()
    }

    pub fn get_path_cost(&self) -> f64 {
        self.path_cost
    }

    pub fn get_depth(&self) -> u32 {
        self.depth
    }

    pub fn next_node(parent_node: Rc<Self>, action: &A) -> Self {
        let new_cost = parent_node.path_cost + action.get_cost();
        let new_depth = parent_node.depth + 1;
        let new_state = parent_node.state.perform_action(action);
        Node { state: new_state, action: Some(action.clone()), parent_node: Some(parent_node), path_cost: new_cost, depth: new_depth }
    }

    pub fn eval(&self) -> f64 {
        self.path_cost + self.state.heuristic()
    }
    
}