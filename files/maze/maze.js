// Maze representation and generation.
var Mazes = (function() {
    function Cell() {
        this.up = true;
        this.down = true;
        this.left = true;
        this.right = true;
    }

    function Maze(width, height) {
        this.width = width;
        this.height = height;

        this.cells = [];
        for (var i = 0; i < width * height; i++) {
            this.cells[i] = new Cell();
        }

        this.at = function(row, column) {
            return this.cells[row * this.width + column];
        }
    }

    function eller(width, height) {
        var maze = new Maze(width, height);

        // For a set of cells i_1, i_2, ..., i_k from left to right that are
        // connected in the previous row R[i_1] = i_2, R[i_2] = i_3, ... and
        // R[i_k] = i_1. Similarly for the left
        var L = [];
        var R = [];

        // At the top each cell is connected only to itself
        for (var c = 0; c < width; c++) {
            L[c] = c;
            R[c] = c;
        }

        // Generate each row of the maze excluding the last
        for (var r = 0; r < height - 1; r++) {
            for (var c = 0; c < width; c++) {
                // Should we connect this cell and its neighbour to the right?
                if (c != width-1 && c+1 != R[c] && Math.random() < 0.5) {
                    R[L[c+1]] = R[c]; // Link L[c+1] to R[c]
                    L[R[c]] = L[c+1];
                    R[c] = c+1; // Link c to c+1
                    L[c+1] = c;

                    maze.at(r, c).right = false;
                    maze.at(r, c+1).left = false;
                }

                // Should we connect this cell and its neighbour below?
                if (c != R[c] && Math.random() < 0.5) {
                    R[L[c]] = R[c]; // Link L[c] to R[c]
                    L[R[c]] = L[c];
                    R[c] = c; // Link c to c
                    L[c] = c;
                } else {
                    maze.at(r, c).down = false;
                    maze.at(r+1, c).up = false;
                }
            }
        }

        // Handle the last row to guarantee the maze is connected
        for (var c = 0; c < width; c++) {
            if (c != width-1 && c+1 != R[c] && (c == R[c] || Math.random() < 0.5)) {
                R[L[c+1]] = R[c]; // Link L[c+1] to R[c]
                L[R[c]] = L[c+1];
                R[c] = c+1; // Link c to c+1
                L[c+1] = c;

                maze.at(height-1, c).right = false;
                maze.at(height-1, c+1).left = false;
            }

            R[L[c]] = R[c]; // Link L[c] to R[c]
            L[R[c]] = L[c];
            R[c] = c; // Link c to c
            L[c] = c;
        }

        // Entrance and exit
        maze.at(0, 0).left = false;
        maze.at(height-1, width-1).right = false;
         
        return maze;
    }

    function dijkstra(maze) {
        var cells = maze.width * maze.height;

        var dist = [];
        var prev = [];

        for (var i = 0; i < cells; i++) {
            dist[i] = cells+1;
            prev[i] = -1;
        }

        dist[0] = 0;

        var q = new Heap(function (x) { return dist[x]; });
        for (var i = 0; i < cells; i++) {
            q.push(i);
        }

        while (q.size() > 0) {
            var u = q.pop();
            // Early out if we have reached the end
            if (u == cells - 1) {
                break;
            }

            var du = dist[u];
            var cell = maze.at(Math.floor(u / maze.width), u % maze.width);

            // Relax the adjacent cells
            var up = u - maze.width;
            if (!cell.up && du+1 < dist[up]) {
                dist[up] = du+1;
                prev[up] = u;
                q.decreaseKey(up);
            }
            var down = u + maze.width;
            if (!cell.down && du+1 < dist[down]) {
                dist[down] = du+1;
                prev[down] = u;
                q.decreaseKey(down);
            }
            var left = u - 1;
            if (!cell.left && du+1 < dist[left]) {
                dist[left] = du+1;
                prev[left] = u;
                q.decreaseKey(left);
            }
            var right = u + 1;
            if (!cell.right && du+1 < dist[right]) {
                dist[right] = du+1;
                prev[right] = u;
                q.decreaseKey(right);
            }
        }

        // Construct the path from the previous links
        var path = [];
        step = cells - 1;
        do {
            path.push([Math.floor(step / maze.width), step % maze.width]);
            step = prev[step];
        } while (step >= 0);
        path.reverse();

        return path;
    }

    function canvasMaze(maze, canvasCtx, cellSize) {
        var drawLine = function(x1, y1, x2, y2) {
            var w = Math.max(Math.abs(x2 - x1), 1);
            var h = Math.max(Math.abs(y2 - y1), 1);
            canvasCtx.fillRect(x1, y1, w, h);
        }

        for (var r = 0; r < maze.height; r++) {
            for (var c = 0; c < maze.width; c++) {
                var cell = maze.at(r, c);
                var x = c * cellSize;
                var y = r * cellSize;
                if (cell.up) {
                    drawLine(x, y, x + cellSize, y);
                }
                if (cell.down) {
                    drawLine(x, y + cellSize, x + cellSize, y + cellSize);
                }
                if (cell.left) {
                    drawLine(x, y, x, y + cellSize);
                }
                if (cell.right) {
                    drawLine(x + cellSize, y, x + cellSize, y + cellSize);
                }
            }
        }
    }

    function canvasPath(path, canvasCtx, cellSize) {
        var drawLine = function(x1, y1, x2, y2) {
            var sx = (x1 < x2) ? x1 : x2;
            var sy = (y1 < y2) ? y1 : y2;
            var w = Math.max(Math.abs(x2 - x1), 1);
            var h = Math.max(Math.abs(y2 - y1), 1);
            canvasCtx.fillRect(sx, sy, w, h);
        }

        var offset = cellSize / 2;

        for (var i = 0; i < path.length - 1; i++) {
            var curr = path[i][0], curc = path[i][1];
            var nextr = path[i+1][0], nextc = path[i+1][1];

            drawLine(curc * cellSize + offset,
                     curr * cellSize + offset,
                     nextc * cellSize + offset,
                     nextr * cellSize + offset);
        }
    }

    return {
        generate: eller,
        solve: dijkstra,
        drawMaze: canvasMaze,
        drawPath: canvasPath
    }
})();
