package com.aggregated;

import java.util.List;
import java.util.Objects;
/**
 * A Trie's implementation
 * works only on lower-cased letters
 */
public class Trie {
    private static final Trie INSTANCE = new Trie();
    /**
     * 26 letters
     */
    private static final int SIZE = 26;
    private TrieNode root;
    private Trie() {
        root = new TrieNode();
    }
    public static Trie current() {
        return INSTANCE;
    }

    public void addWord(List<String> stringList) {
        for (String each : stringList) {
            addWord(each.toLowerCase());
        }
    }
    public void addWord(String inp) {
        inp = inp.toLowerCase();
        if (inp.contains(" ")) {
            addWord(StringUtils.makeNonAlphaStringsFrom(inp));
            return;
        }
        TrieNode current = root;
        for (Character each : inp.toCharArray()) {
            final int idx = StringUtils.asciiValueOf(each, Boolean.TRUE);
            if (Objects.isNull(current.trieNodes[idx])) {
                current.trieNodes[idx] = new TrieNode();
            }
            current = current.trieNodes[idx];
        }
        current.count++;
    }
    public int searchWord(String inp) {
        TrieNode current = root;
        for (Character each : inp.toCharArray()) {
            final int idx = StringUtils.asciiValueOf(each, Boolean.TRUE);
            if (Objects.isNull(current.trieNodes[idx])) {
                break;
            }
            current = current.trieNodes[idx];
        }
        return current.count;
    }
    class TrieNode {
        TrieNode[] trieNodes;
        int count;
        private TrieNode() {
            count = 0;
            trieNodes = new TrieNode[SIZE];
            for (int i = 0; i < SIZE; i++) {
                trieNodes[i] = null;
            }
        }
    }
}
