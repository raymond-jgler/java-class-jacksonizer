package com.aggregated;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoubleEndedStack<T> {
  private int top = -1;
  private Optional<Integer> initialCap;
  private List<T> elements;
  public DoubleEndedStack(int n) {
    this.initialCap = Optional.of(n);
    this.elements = new ArrayList<>(n);
  }
  public DoubleEndedStack() {
    this.initialCap = Optional.empty();
    this.elements = new ArrayList<>();
  }
  public void setInitialCap(int initialCap) {
    this.initialCap = Optional.of(initialCap);
  }
  public void push(T ele) {
    if (this.initialCap.isPresent() && top == this.initialCap.get()) {
      throw new RuntimeException("Stack size exceeds the limit !");
    }
    this.elements.add(ele);
    top++;
  }
  public T pop() {
    if (isEmpty()) {
      return null;
    }
    final T toReturn = this.elements.get(this.top);
    this.elements.remove(this.top);
    this.top--;
    return toReturn;
  }
  public void bringTopToBottom(T dummy) {
    if (this.elements.size() < 2) {
      return;
    }
    final T btmVal = pop();
    push(dummy); //add a dummy val to increase the size..
    int backwardIdx = top;
    while (backwardIdx > 0) {
      this.elements.set(backwardIdx, this.elements.get(backwardIdx - 1));
      backwardIdx--;
    }
    this.elements.set(backwardIdx, btmVal);
  }
  public void dynamicPop(int ordinal) {
    int times = this.size() - ordinal;
    for (int i = 0; i < times; i++) {
      bringTopToBottom((T) Optional.empty());
    }
  }
  public int size() {
    return this.elements.size();
  }
  public boolean isEmpty() {
    return top == -1 || this.elements.size() == 0;
  }
  public boolean contains(T ele) {
    return this.elements.contains(ele);
  }
}














