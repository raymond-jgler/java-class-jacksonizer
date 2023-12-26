package com.aggregated;

import java.util.List;
public class TrieRepository {
    private static final Trie trie = Trie.current();
    private static final TrieRepository INSTANCE = new TrieRepository();
    private String mode;
    private TrieRepository() {
    }
    public static TrieRepository go() {
        return INSTANCE;
    }
    public TrieRepository resetTrie() {
        this.trie.reset();
        return this;
    }
    public boolean containsData() {
        return trie.containsData();
    }
    public TrieRepository with(String inp, boolean isConcatenate) {
        if (isConcatenate) {
            trie.addWord(inp);
        } else {
            trie.addWord(StringUtils.makeNonAlphaStringsFrom(inp, Boolean.TRUE));
        }
        return this;
    }
    public TrieRepository with(List<String> inp) {
        trie.addWord(inp);
        return this;
    }
    public int search(String inp) {
        if (StringUtils.isEmpty(inp)) {
            return -1;
        }
        return trie.searchWord(inp.toLowerCase());
    }
    public static void main(String[] args) {
        int result = TrieRepository.go()
                .with("hello hi there hello i am here", false)
                .search("hello");

        System.out.println(result);
    }
}
