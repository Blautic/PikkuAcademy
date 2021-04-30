package com.blautic.pikkuacademysnake.data;

import android.graphics.Color;

public class GridSquare {
  private int mType;//Tipo de elemento

  public GridSquare(int type) {
    mType = type;
  }

  public int getColor() {
    switch (mType) {
      case GameType.GRID://Espacio en blanco
        return Color.parseColor("#2d2d2d");
      case GameType.FOOD://food
        return  Color.parseColor("#ba4f24");
      case GameType.SNAKE://snake
        return Color.parseColor("#cbca22");
    }
    return Color.WHITE;
  }

  public void setType(int type) {
    mType = type;
  }
}
