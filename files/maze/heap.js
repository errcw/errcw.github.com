// A binary minimum heap.
function Heap(valfn) {
    this.heap = [];
    this.valfn = valfn;
}

Heap.prototype = {
    push: function(element) {
        this.heap.push(element);
        this._siftUp(this.heap.length - 1);
    },

    pop: function() {
        var result = this.heap[0];
        var end = this.heap.pop();
        if (this.heap.length > 0) {
            this.heap[0] = end;
            this._siftDown(0);
        }
        return result;
    },

    decreaseKey: function(element) {
        for (var i = 0; i < this.heap.length; i++) {
            if (this.heap[i] == element) {
                this._siftUp(i);
                return;
            }
        }
        throw new Error("Element not found");
    },

    size: function() {
        return this.heap.length;
    },

    _siftDown: function(node) {
        while (true) {
            var smallest = node;
            if (2*node+1 <= this.heap.length && this._value(2*node+1) < this._value(node)) {
                smallest = 2*node+1;
            }
            if (2*node+2 <= this.heap.length && this._value(2*node+2) < this._value(smallest)) {
                smallest = 2*node+2;
            }
            if (smallest != node) {
                this._exchange(node, smallest);
                node = smallest;
            } else {
                break;
            }
        }
    },

    _siftUp: function(node) {
        while (node > 0) {
            var par = Math.floor((node - 1) / 2);
            if (this._value(node) < this._value(par)) {
                this._exchange(node, par);
                node = par;
            } else {
                break;
            }
        }
    },

    _exchange: function(nodea, nodeb) {
        var t = this.heap[nodea];
        this.heap[nodea] = this.heap[nodeb];
        this.heap[nodeb] = t;
    },

    _value: function(node) {
        return this.valfn(this.heap[node]);
    }
};
