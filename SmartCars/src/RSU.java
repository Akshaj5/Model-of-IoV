
class RSU {
    private Position position;
    private App.Buffer buffer;

    Position getPosition() {
        return position;
    }

    App.Buffer getBuffer(){
        return buffer;
    }

    RSU(int x, int y, App.Buffer buff) {
        position = new Position(x, y);
        this.buffer = buff;
    }
}
