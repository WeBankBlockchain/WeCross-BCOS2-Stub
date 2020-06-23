package com.webank.wecross.stub.bcos.abi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedType {

    private String name;
    private String type;
    private boolean indexed;
    private Type typeObj;

    public Type getTypeObj() {
        return typeObj;
    }

    public void setTypeObj(Type typeObj) {
        this.typeObj = typeObj;
    }

    public NamedType() {}

    public NamedType(String name, String type) {
        this.name = name.trim();
        this.type = type.trim();
        this.setTypeObj(new Type(type));
    }

    public NamedType(String name, String type, boolean indexed) {
        this.name = name;
        this.type = type;
        this.indexed = indexed;
        this.setTypeObj(new Type(type));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public static class Type {
        public String name;
        public String baseName;
        public List<Integer> dimensions = new ArrayList<Integer>();

        public Type(String name) {
            int index = name.indexOf("[");
            this.baseName = (-1 == index) ? name.trim() : name.substring(0, index);
            this.name = name;
            this.initialize();
        }

        private void initialize() {
            Pattern p = Pattern.compile("\\[[0-9]{0,}\\]");
            Matcher m = p.matcher(name);
            while (m.find()) {
                String s = m.group();
                String dig = s.substring(s.indexOf("[") + 1, s.indexOf("]")).trim();
                if (dig.isEmpty()) {
                    dimensions.add(0);
                } else {
                    dimensions.add(Integer.valueOf(dig));
                }
            }
        }

        @Override
        public String toString() {
            return "Type{"
                    + "name='"
                    + name
                    + '\''
                    + ", baseName='"
                    + baseName
                    + '\''
                    + ", dimensions="
                    + dimensions
                    + '}';
        }

        public String getName() {
            return name;
        }

        public String getBaseName() {
            return baseName;
        }

        public boolean isList() {
            return !dimensions.isEmpty();
        }

        public boolean isDynamicList() {
            if (isList()) {
                for (Integer dimension : dimensions) {
                    if (dimension == 0) {
                        return true;
                    }
                }
            }

            return false;
        }

        public int multiDimension() {
            if (isList() && !isDynamicList()) {
                Integer result = 1;
                for (Integer dimension : dimensions) {
                    result *= dimension;
                }

                return result;
            }

            return 0;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setBaseName(String baseName) {
            this.baseName = baseName;
        }

        public List<Integer> getDimensions() {
            return dimensions;
        }

        public void setDimensions(List<Integer> dimensions) {
            this.dimensions = dimensions;
        }
    }
}
