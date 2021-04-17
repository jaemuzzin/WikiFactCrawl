package info.clock.factcrawl;

import java.util.Objects;

/**
 *
 * @author Jae
 */
public class Item {

        public Item(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + Objects.hashCode(this.id);
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
            final Item other = (Item) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            return true;
        }

        public String id;
        public String label;

        @Override
        public String toString() {
            return id;
        }

    }
