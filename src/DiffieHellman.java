import java.math.BigInteger;

public class DiffieHellman {
    private BigInteger p = new BigInteger("D49A7AD853F484570E1811CC99D285D3DB6BEA6EF8ECF6D245058590D8EAA7861A512AD05B5416033AF237970E32D4ACB3B271B1009D96F4237C35781A54F7EFD66F7C06A125C21023A270213908836132C9D41151634E45C957018A233A5919C5BAFD9EBE3351F84E5F5623B3C84AA92004399E8137AC8D0D2F2A7C9A38BB57", 16);
    private BigInteger g = new BigInteger("2");

    public BigInteger getP(){
        return p;
    }

    public BigInteger getG(){
        return g;
    }

    public BigInteger calcularmodp(BigInteger x){
        return g.modPow(x, p);
    }

    public BigInteger calcularz(BigInteger y, BigInteger x) {
        return y.modPow(x, p);
    }
}
