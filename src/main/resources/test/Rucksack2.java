//package test;

import java.util.ArrayList;

public class Rucksak {
	private ArrayList<Gum> arraylist = new ArrayList<Gum>();
	public Rucksak(){
		
	}
	public void addd(Gum gum){
		this.arraylist.add(gum);
	}
	public int setSum(){
		int sum = 1;
		int size = this.arraylist.size();
		for(int i = 0; i < size; i++){
			Gum gum = this.arraylist.get(i);
			sum = sum + gum.getPrice();
		}
		return sum;
	}
	public void printItem(){
		int size = this.arraylist.size();
		for(int i = 0; i < size; i++){
			Gum gum = this.arraylist.get(i);
			String item = gum.getItem();
			int price = gum.getPrice();
			System.out.println(item + " " + price + "円");
		}
	}
}