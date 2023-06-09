public class Node {

    Node prev;              // previous Node in a doubly-linked list
    Node next;              // next Node in a doubly-linked list
    //ProcessLogModel logModel;
    String processLine;
    //public char data;       // data stored in this Node

    Node()
    {                // constructor for head Node
        prev = this;           // of an empty doubly-linked list
        next = this;

        processLine = "New";

    }

    Node(String str)
    {       // constructor for a Node with data
        prev = null;
        next = null;
        processLine = str;
        //this.data = data;     // set argument data to instance variable data
    }

    public void Append(Node newNode)
    {  // attach newNode after this Node
        newNode.prev = this;
        newNode.next = next;
        if (next != null)
        {
            next.prev = newNode;
        }
        next = newNode;
//        System.out.println("Node with data " + newNode.logModel.barCode
//                + " appended after Node with data " + logModel.barCode);
    }

    public void Insert(Node newNode)
    {  // attach newNode before this Node
        newNode.prev = prev;
        newNode.next = this;
        prev.next = newNode;;
        prev = newNode;
//        System.out.println("Node with data " + newNode.logModel.barCode
//                + " inserted before Node with data " + logModel.barCode);
    }

//    public void Remove()
//    {              // remove this Node
//        next.prev = prev;                 // bypass this Node
//        prev.next = next;
//        System.out.println("Node with data " + logModel.barCode + " removed");
//    }
//    public String toString(){
//        return this.logModel.barCode + " - " + this.logModel.barCode;
//    }
}

