package tasktracker.managers;

import tasktracker.interfaces.HistoryManager;
import tasktracker.storage.*;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node<Task>> viewHistory;
    private Node<Task> head;
    private Node<Task> tail;

    private static class Node<T> {
        T data;
        Node<T> before;
        Node<T> after;

        private Node(T data) {
            this.data = data;
            this.before = null;
            this.after = null;
        }
    }

    public InMemoryHistoryManager() {
        this.viewHistory = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (Objects.nonNull(task)) {
            if (viewHistory.containsKey(task.getId())) {
                removeNode(viewHistory.get(task.getId()));
                viewHistory.remove(task.getId());
            }
            linkLast(task.getTaskForHistory());
            viewHistory.put(task.getId(), tail);
        }
    }

    private void linkLast(Task task) {
        Node<Task> node = new Node<>(task);
        if (tail == null) {
            head = node;
        } else {
            tail.after = node;
            node.before = tail;
        }
        tail = node;
    }

    private void removeNode(Node<Task> node) {
        if (Objects.nonNull(node)) {
            Node<Task> prevNode = node.before;
            Node<Task> nextNode = node.after;

            if (prevNode == null)
                head = nextNode;
            else
                prevNode.after = nextNode;

            if (nextNode == null)
                tail = prevNode;
            else
                nextNode.before = prevNode;
        }
    }

    @Override
    public void remove(int id) {
        if (viewHistory.containsKey(id)) {
            removeNode(viewHistory.get(id));
            viewHistory.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> result = new ArrayList<>();
        Node<Task> node = head;
        while (node != null) {
            result.add(node.data);
            node = node.after;
        }
        return result;
    }
}