package taskTracker.managers;

import taskTracker.interfaces.HistoryManager;
import taskTracker.storage.*;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> viewHistory;
    private Node head;
    private Node tail;

    private class Node {
        Task task;
        Node before;
        Node after;

        private Node(Task task) {
            this.task = task;
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
        Node node = new Node(task);
        if (tail == null) {
            head = node;
        } else {
            tail.after = node;
            node.before = tail;
        }
        tail = node;
    }

    private void removeNode(Node node) {
        if (Objects.nonNull(node)) {
            Node prevNode = node.before;
            Node nextNode = node.after;

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
        Node node = head;
        while (node != null) {
            result.add(node.task);
            node = node.after;
        }
        return result;
    }
}