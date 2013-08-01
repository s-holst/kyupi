
module c17 ( X1gatX, X2gatX, X3gatX, X6gatX, X7gatX, X22gatX, X23gatX );
  input X1gatX, X2gatX, X3gatX, X6gatX, X7gatX;
  output X22gatX, X23gatX;
  wire   n2;

  NAND2X0 U4 ( .IN1(X3gatX), .IN2(X6gatX), .QN(n2) );
  AO22X1 U5 ( .IN1(X3gatX), .IN2(X1gatX), .IN3(X2gatX), .IN4(n2), .Q(X22gatX)
         );
  OA21X1 U6 ( .IN1(X2gatX), .IN2(X7gatX), .IN3(n2), .Q(X23gatX) );
endmodule

