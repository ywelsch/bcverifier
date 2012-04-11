package obool;

public class OBool {
  private Bool g;
  public OBool() { g = new Bool(); g.set(false); }
  public void setg(boolean b) { g.set(b); }
  public boolean getg() { return g.get(); }
}