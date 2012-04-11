package obool;

public class OBool {
  private Bool g;
  public OBool() { g = new Bool(); g.set(true); }
  public void setg(boolean b) { g.set(!b); }
  public boolean getg() { return !g.get(); }
}