/*
package alix.common.scheduler.tasks;

import alix.common.utils.collections.queue.AlixDeque;

public final class AlixTaskList {

    private final AlixDeque<Runnable> deque = new AlixDeque<>();

    public synchronized void add(Runnable task) {//sync because of multi-thread access
        this.deque.offerLast(task);//add the task
    }

    //not synchronized for the sake of speed with an extremely unlikely task miss chance
    public void executeAllAndClear() { //has an extremely low chance of missing a task execution - the new task object would need to take longer adding than the creation of the Consumer and execution of the first task, making it pretty much impossible
        if (this.deque.isEmpty()) return;

        //no need for locks, because the <Node> object itself is updated at task addition
        AlixDeque.Node<Runnable> node = this.deque.firstNode(); //get the first node, as all of the other nodes are attached to it
        this.deque.clear(); //clear the deque to prevent elements from being added as the tasks are being executed

        AlixDeque.forEach(Runnable::run, node); //execute the tasks synchronously
    }
}*/
