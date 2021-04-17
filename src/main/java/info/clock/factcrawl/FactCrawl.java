package info.clock.factcrawl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 *
 * @author Jae
 */
public class FactCrawl {

    public class SortedItem implements Comparable<SortedItem> {

        int priority = 0;
        public Item item;

        public SortedItem(String id, String label, int priority) {
            item = new Item(id, label);
            this.priority = priority;
        }

        public SortedItem(Item item, int priority) {
            this.item = item;
            this.priority = priority;
        }

        @Override
        public int compareTo(SortedItem o) {
            return (priority - o.priority);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + this.priority;
            hash = 41 * hash + Objects.hashCode(this.item);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SortedItem other = (SortedItem) obj;
            if (this.priority != other.priority) {
                return false;
            }
            if (!Objects.equals(this.item, other.item)) {
                return false;
            }
            return true;
        }

    }

    public class Fact {

        public Fact() {
        }

        public Fact(String subject, String property, String object) {
            this.subject = subject;
            this.property = property;
            this.object = object;
        }

        @Override
        public String toString() {
            return subject + " " + property + ": " + object;
        }

        public String subject;
        public String property;
        public String object;
    }
    private LinkedList<Item> visited = new LinkedList<>();
    private PriorityQueue<SortedItem> items = new PriorityQueue<>();
    private LinkedList<Fact> facts = new LinkedList<>();
    private SortedItem root;
    private Wikidata wiki = new Wikidata();
    private int maxDistance = 5;

    public FactCrawl(String root, int maxDistance) {
        this.root = new SortedItem(root, "root", 0);
        items.add(this.root);
        this.maxDistance = maxDistance;
    }

    private void addToItems(Collection<SortedItem> toadd) {
        toadd.stream()
                .filter(s -> !items.contains(s))
                .filter(s -> !visited.contains(s))
                .filter(s -> s.priority <= maxDistance)
                .forEach(s -> items.offer(s));
    }

    public void crawlComplete() {
        //no point parallel processing here as wikidata has 5 concurrent request limit
        while (!items.isEmpty()) {
            crawl(items.poll());
        }
    }

    public boolean crawl(SortedItem item) {
        if (item != null) {
            visited.add(item.item);
            Thread t1 = new Thread(()
                    -> {
                int itemsSize = items.size();
                addToItems(wiki.getSynonyms(item.item).stream().map(i -> new SortedItem(i, item.priority + 1)).collect(Collectors.toList()));
                System.err.println("Found " + (items.size() - itemsSize) + " new synonyms of " + item.item.label);
            }
            );
            t1.start();
            Thread t2 = new Thread(()
                    -> {
                int itemsSize = items.size();
                addToItems(wiki.getMembersOf(item.item).stream().map(i -> new SortedItem(i, item.priority + 1)).collect(Collectors.toList()));
                System.err.println("Found " + (items.size() - itemsSize) + " new members of " + item.item.label);
            }
            );
            t2.start();
            Thread t3 = new Thread(()
                    -> {
            int itemsSize = items.size();
            items.addAll(wiki.getLists(item.item).stream().map(i -> new SortedItem(i, i, item.priority + 1)).collect(Collectors.toList()));
            System.err.println("Found " + (items.size() - itemsSize) + " new lists in " + item.item.label);
            }
            );
            t3.start();
            wiki.getProperties(item.item).entrySet().stream()
                    .map(e -> new Fact(item.item.label, e.getKey(), e.getValue()))
                    .forEach(f -> facts.addLast(f));
            try {
                t1.join();
                t2.join();
                t3.join();
            } catch (InterruptedException ie) {

            }
            return true;
        } else {
            return false;
        }
    }

    public LinkedList<Fact> getFacts() {
        return facts;
    }
}
