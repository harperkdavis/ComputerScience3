// Author: Harper Davis

use std::collections::{VecDeque, BinaryHeap};

use crate::{state::State, action::Action, node::Node};

pub trait Frontier<S: State<Action = A>, A: Action> {
    fn new() -> Self where Self: Sized;
    fn clear(&mut self);
    fn is_empty(&self) -> bool;
    fn insert(&mut self, node: Node<S, A>);
    fn pop(&mut self) -> Option<Node<S, A>>;
    fn size(&self) -> usize;
}

pub struct QueueFrontier<S: State<Action = A>, A: Action> {
    queue: VecDeque<Node<S, A>>,
}

impl <S: State<Action = A>, A: Action> Frontier<S, A> for QueueFrontier<S, A> {

    fn new() -> QueueFrontier<S, A> {
        QueueFrontier { queue: VecDeque::new() }
    }

    fn clear(&mut self) {
        self.queue.clear();
    }

    fn is_empty(&self) -> bool {
        self.queue.is_empty()
    }

    fn insert(&mut self, node: Node<S, A>) {
        self.queue.push_back(node);
    }

    fn pop(&mut self) -> Option<Node<S, A>> {
        self.queue.pop_front()
    }

    fn size(&self) -> usize {
        self.queue.len()
    }

}

pub struct StackFrontier<S: State<Action = A>, A: Action> {
    stack: VecDeque<Node<S, A>>,
}

impl <'a, S: State<Action = A>, A: Action> Frontier<S, A> for StackFrontier<S, A> {

    fn new() -> StackFrontier<S, A> {
        StackFrontier { stack: VecDeque::new() }
    }

    fn clear(&mut self) {
        self.stack.clear();
    }

    fn is_empty(&self) -> bool {
        self.stack.is_empty()
    }

    fn insert(&mut self, node: Node<S, A>) {
        self.stack.push_back(node);
    }

    fn pop(&mut self) -> Option<Node<S, A>> {
        self.stack.pop_back()
    }

    fn size(&self) -> usize {
        self.stack.len()
    }

}

pub struct PriorityQueueFrontier<S: State<Action = A>, A: Action> {
    queue: BinaryHeap<Node<S, A>>
}

impl <S: State<Action = A>, A: Action> Frontier<S, A> for PriorityQueueFrontier<S, A> {

    fn new() -> PriorityQueueFrontier<S, A> {
        PriorityQueueFrontier { queue: BinaryHeap::new() }
    }

    fn clear(&mut self) {
        self.queue.clear();
    }

    fn is_empty(&self) -> bool {
        self.queue.is_empty()
    }

    fn insert(&mut self, node: Node<S, A>) {
        self.queue.push(node);
    }

    fn pop(&mut self) -> Option<Node<S, A>> {
        self.queue.pop()
    }

    fn size(&self) -> usize {
        self.queue.len()
    }

}