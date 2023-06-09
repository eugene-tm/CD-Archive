public class DoublyLinkedList {
    Node head;

    public DoublyLinkedList()
    {
        head = new Node();
    }

    public DoublyLinkedList(String str)
    {
        head = new Node(str);
    }

//    public Node Find(String log)
//    {          // find Node containing x
//        for (Node current = head.next; current != head; current = current.next)
//        {
//            if (current.logModel.barCode.compareToIgnoreCase(log) == 0)
//            {        // is x contained in current Node?
//                System.out.println("Data " + log + " found");
//                return current;               // return Node containing x
//            }
//        }
//        System.out.println("Data " + log + " not found");
//        return null;
//    }


    public Node Get(int i)
    {
        Node current = this.head;
        if (i < 0 || current == null)
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        while (i > 0)
        {
            i--;
            current = current.next;
            if (current == null)
            {
                throw new ArrayIndexOutOfBoundsException();
            }
        }
        return current;
    }

    public String toString() {
        if (head.next == head) { // list is empty, only header Node
            return "List Empty";
        }

        StringBuilder sb = new StringBuilder();
        Node current = head.next;
        while (current != head && current != null) {
            sb.append(current.processLine);
            current = current.next;
        }
        return sb.toString();
    }


//    public void Print()
//    {                  // print content of list
//        if (head.next == head)
//        {             // list is empty, only header Node
//            System.out.println("list empty");
//            return;
//        }
//        System.out.print("list content = ");
//        for (Node current = head.next; current != head; current = current.next)
//        {
//            System.out.print(" " + current.logModel.barCode);
//        }
//        System.out.println("");
//    }

//    public static void main(String[] args) {
//        DList dList = new DList();              // create an empty dList
//        dList.print();
//
//        dList.head.append(new Node(processLine));       // add Node with data '1'
//        txaProcessLog.setText(DoublyLinedList.toString();
//        dList.head.append(new Node("3", "4"));       // add Node with data '2'
//        dList.print();
//        dList.head.append(new Node("5","6"));       // add Node with data '3'
//        dList.print();
//        dList.head.insert(new Node("A","B"));       // add Node with data 'A'
//        dList.print();
//        dList.head.insert(new Node("C","D"));       // add Node with data 'B'
//        dList.print();
//        dList.head.insert(new Node("E","F"));       // add Node with data 'C'
//        dList.print();
//
//        Node nodeA = dList.find("A");           // find Node containing 'A'
//        nodeA.remove();                         // remove that Node
//        dList.print();
//
//        Node node2 = dList.find("3");           // find Node containing '2'
//        node2.remove();                           // remove that Node
//        dList.print();
//
//        Node nodeB = dList.find("5");            // find Node containing 'B'
//        nodeB.append(new Node("Linked","List"));   // add Node with data X
//        dList.print();
//    }
}
