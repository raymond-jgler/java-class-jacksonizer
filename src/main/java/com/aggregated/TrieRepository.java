package com.aggregated;

import java.util.List;
public class TrieRepository {
    private static final Trie trie = Trie.current();
    private static final TrieRepository INSTANCE = new TrieRepository();
    private TrieRepository(){}
    public static TrieRepository go() {
        return INSTANCE;
    }
    public TrieRepository with(String inp) {
        trie.addWord(inp);
        return this;
    }
    public TrieRepository with(List<String> inp) {
        trie.addWord(inp);
        return this;
    }
    public int search(String inp) {
        return trie.searchWord(inp);
    }
    public static void main(String[] args) {
        int result = TrieRepository.go()
                .with("hello hi there hello i am here")
                .search("hello");

        System.out.println(result);
    }
}
