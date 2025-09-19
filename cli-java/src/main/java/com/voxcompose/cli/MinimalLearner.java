package com.voxcompose.cli;

import java.util.*;

public final class MinimalLearner {
    private MinimalLearner() {}

    private static final String[][] ESSENTIAL_CAPS = new String[][]{
            {"api", "API"},
            {"json", "JSON"},
            {"http", "HTTP"},
            {"url", "URL"},
            {"github", "GitHub"},
            {"nodejs", "Node.js"},
            {"postgresql", "PostgreSQL"},
            {"kubernetes", "Kubernetes"},
            {"docker", "Docker"},
            {"redis", "Redis"},
            {"graphql", "GraphQL"},
            {"rest", "REST"}
    };

    private static final String[][] BASIC_SPLITS = new String[][]{
            {"pushto", "push to"},
            {"committhis", "commit this"},
            {"followup", "follow up"},
            {"setup", "set up"},
            {"signin", "sign in"},
            {"signout", "sign out"},
            {"login", "log in"},
            {"logout", "log out"},
            {"frontend", "front end"},
            {"backend", "back end"},
            {"dropdown", "drop down"},
            {"builtin", "built in"}
    };

    public static boolean applyLearning(String text, LearningProfile profile) {
        if (text == null || text.isBlank()) return false;
        boolean changed = false;
        String lower = text.toLowerCase(Locale.ROOT);

        Map<String, String> caps = profile.getCapitalizations();
        Map<String, String> splits = profile.getWordCorrections();
        Set<String> vocab = new LinkedHashSet<>(profile.getTechnicalVocabulary());

        for (String[] pair : ESSENTIAL_CAPS) {
            String low = pair[0];
            String proper = pair[1];
            if (lower.contains(low) && !proper.equals(caps.get(low))) {
                caps.put(low, proper);
                vocab.add(proper);
                changed = true;
            }
        }

        for (String[] pair : BASIC_SPLITS) {
            String bad = pair[0];
            String good = pair[1];
            if (lower.contains(bad) && !good.equals(splits.get(bad))) {
                splits.put(bad, good);
                changed = true;
            }
        }

        if (changed) {
            List<String> sorted = new ArrayList<>(vocab);
            Collections.sort(sorted, Comparator.naturalOrder());
            profile.setTechnicalVocabulary(sorted);
        }
        return changed;
    }
}
