package b2bpl.bytecode;


public class BCField extends BCMember {

  public static final BCField[] EMPTY_ARRAY = new BCField[0];

  private final String name;

  private final JType type;

  private final String descriptor;

  private final boolean isGhostField;

  public BCField(
      int accessModifiers,
      JClassType owner,
      String name,
      JType type) {
    this(accessModifiers, owner, name, type, false);
  }

  public BCField(
      int accessModifiers,
      JClassType owner,
      String name,
      JType type,
      boolean isGhostField) {
    super(accessModifiers, owner);
    this.name = name;
    this.type = type;
    this.descriptor = type.getDescriptor();
    this.isGhostField = isGhostField;
  }

  public String getName() {
    return name;
  }

  public JType getType() {
    return type;
  }

  public String getDescriptor() {
    return descriptor;
  }

  public boolean isGhostField() {
    return isGhostField;
  }

  public String getQualifiedName() {
    return owner.getName() + "." + name;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append(type.getName());
    sb.append(' ');
    sb.append(name);

    return sb.toString();
  }

@Override
public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
            + ((descriptor == null) ? 0 : descriptor.hashCode());
    result = prime * result + (isGhostField ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
}

@Override
public boolean equals(Object obj) {
    if (this == obj) {
        return true;
    }
    if (obj == null) {
        return false;
    }
    if (getClass() != obj.getClass()) {
        return false;
    }
    BCField other = (BCField) obj;
    if (descriptor == null) {
        if (other.descriptor != null) {
            return false;
        }
    } else if (!descriptor.equals(other.descriptor)) {
        return false;
    }
    if (isGhostField != other.isGhostField) {
        return false;
    }
    if (name == null) {
        if (other.name != null) {
            return false;
        }
    } else if (!name.equals(other.name)) {
        return false;
    }
    if (type == null) {
        if (other.type != null) {
            return false;
        }
    } else if (!type.equals(other.type)) {
        return false;
    }
    return true;
}
}
