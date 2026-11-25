package datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;
import service.ServiceRequest;

/** Linked-list based Queue (FIFO) that holds pending service requests. */
public class RequestQueue implements Iterable<ServiceRequest> {

    private static class Node {
        ServiceRequest request;
        Node next;

        Node(ServiceRequest request) {
            this.request = request;
        }
    }

    private Node front;
    private Node rear;
    private int size;
    private int lastIssuedNumber = 1000;

    public void enqueue(ServiceRequest request) {
        Node node = new Node(request);
        if (rear == null) {
            front = node;
            rear = node;
        } else {
            rear.next = node;
            rear = node;
        }
        size++;
    }

    /** Removes and returns the oldest request, or null if the queue is empty. */
    public ServiceRequest dequeue() {
        if (front == null) {
            return null;
        }
        ServiceRequest request = front.request;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        size--;
        return request;
    }

    public ServiceRequest peek() {
        return front == null ? null : front.request;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public int size() {
        return size;
    }

    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }

    /** Creates a new unique request ID (numbers are never reused, even after processing). */
    public String generateId() {
        lastIssuedNumber++;
        return "REQ" + lastIssuedNumber;
    }

    public int getLastIssuedNumber() {
        return lastIssuedNumber;
    }

    public void setLastIssuedNumber(int number) {
        if (number > lastIssuedNumber) {
            lastIssuedNumber = number;
        }
    }

    @Override
    public Iterator<ServiceRequest> iterator() {
        return new Iterator<ServiceRequest>() {
            private Node current = front;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public ServiceRequest next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                ServiceRequest request = current.request;
                current = current.next;
                return request;
            }
        };
    }
}
