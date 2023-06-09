public class BTNode {
    int key;
    String name;

    BTNode leftChild;
    BTNode rightChild;

    BTNode(int key, String name) {

        this.key = key;
        this.name = name;
    }

    public String toString() {
        return name + " has the key " + key;
    }
}
