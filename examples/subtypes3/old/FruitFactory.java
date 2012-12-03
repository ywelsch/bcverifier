public class FruitFactory {
  public static Fruit 
    giveMeAFruit(boolean b) {
    if (b)
      return new Apple();
    else
      return new Banana();
  }
}