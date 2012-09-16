package com.mindframe.dealwithit;

import android.graphics.Bitmap;


public class Glasses {

	Bitmap glasses;
	float distance;
	int pos_x;
	int pos_y;
	int dynamic_y = 0;
	String num;

	public Glasses(){
		super();
	}

	public Glasses(Bitmap glasses, float distance, int pos_x, int pos_y) {
		super();
		this.glasses = glasses;
		this.distance = distance;
		this.pos_x = pos_x;
		this.pos_y = pos_y;
	}

	public Bitmap getGlasses() {
		return glasses;
	}

	public void setGlasses(Bitmap glasses) {
		this.glasses = glasses;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public int getPos_x() {
		return pos_x;
	}

	public void setPos_x(int pos_x) {
		this.pos_x = pos_x;
	}

	public int getPos_y() {
		return pos_y;
	}

	public void setPos_y(int pos_y) {
		this.pos_y = pos_y;
	}
	
	
	public boolean equals(Glasses glasses){
		
		if(this.pos_x == glasses.pos_x &&
		   this.pos_y == glasses.pos_y &&
		   this.distance == glasses.distance){
			   return true;
		   }else{
			   return false;
		   }
	}
	
}
