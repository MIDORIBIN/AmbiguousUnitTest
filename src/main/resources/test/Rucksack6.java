//package test;

import java.util.ArrayList;

public class Rucksack{
	private ArrayList<Gum> list = new ArrayList<Gum>();
	public Rucksack(){
		
	}
	public void add(Gum gum){
		this.list.add(gum);
	}
	public int getSum(){
		int sum = 0;
		int size = this.list.size();
		for(int i = 0; i < size; i++){
			Gum gum = this.list.get(i);
			sum = sum + gum.getPrice();
		}
		return sum;
	}
	public void printItems(){
		int size = this.list.size();
		for(int i = 0; i < size; i++){
			Gum gum = this.list.get(i);
			String item = gum.getItem();
			int price = gum.getPrice();
			System.out.println(item + " " + price + "円");
		}
	}
}