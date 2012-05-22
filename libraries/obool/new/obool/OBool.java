package obool;

public class OBool {
  private Bool g = new Bool();
  public OBool() { g.set(true); }
  public void setg(boolean b) { g.set(!b); }
  public boolean getg() { return !g.get(); }
}