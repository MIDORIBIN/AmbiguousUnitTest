//package test;

import java.util.ArrayList;

public class Rucksack{
	private ArrayList<Gum> arrayList = new ArrayList<Gum>();
	public Rucksack(){
		
	}
	public void add(Gum gum){
		this.arrayList.add(gum);
	}
	public int getSum(){
//		int sum = 0;
//		int size = this.arrayList.size();
//		for(int i = 0; i < size; i++){
//			Gum gum = this.arrayList.get(i);
//			sum = sum + gum.getPrice();
//		}
//		return sum;
		return 0;
	}
//	public void printItems(){
//		int size = this.arrayList.size();
//		for(int i = 0; i < size; i++){
//			Gum gum = this.arrayList.get(i);
//			String item = gum.getItem();
//			int price = gum.getPrice();
//			System.out.println(item + " " + price + "円");
//		}
//	}
}