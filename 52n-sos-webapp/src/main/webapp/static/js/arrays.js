/*
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
if (!Array.prototype.forEach) {
    Array.prototype.forEach = function forEach(callback, thisArg) {
        var T, k;
        if (this == null) {
            throw new TypeError("this is null or not defined");
        }
        var O = Object(this);
        var len = O.length >>> 0; // Hack to convert O.length to a UInt32
        if ({}.toString.call(callback) !== "[object Function]") {
            throw new TypeError(callback + " is not a function");
        }
        if (thisArg) {
            T = thisArg;
        }
        k = 0;
        while (k < len) {
            var kValue;
            if (Object.prototype.hasOwnProperty.call(O, k)) {
                kValue = O[k];
                callback.call(T, kValue, k, O);
            }
            k++;
        }
    };
}

if (!Array.prototype.map) {
    Array.prototype.map = function(callback, thisArg) {
        var T, A, k;
        if (this == null) {
            throw new TypeError(" this is null or not defined");
        }
        var O = Object(this);
        var len = O.length >>> 0;
        if ({}.toString.call(callback) != "[object Function]") {
            throw new TypeError(callback + " is not a function");
        }
        if (thisArg) {
            T = thisArg;
        }
        A = new Array(len);
        k = 0;
        while (k < len) {
            var kValue, mappedValue;
            if (k in O) {
                kValue = O[k];
                mappedValue = callback.call(T, kValue, k, O);
                A[k] = mappedValue;
            }
            k++;
        }
        return A;
    };
}

if (!Array.prototype.filter) {
    Array.prototype.filter = function(fun /*, thisp */ ) {
        "use strict";
        if (this == null) throw new TypeError();
        var t = Object(this);
        var len = t.length >>> 0;
        if (typeof fun != "function") throw new TypeError();
        var res = [];
        var thisp = arguments[1];
        for (var i = 0; i < len; i++) {
            if (i in t) {
                var val = t[i];
                if (fun.call(thisp, val, i, t)) res.push(val);
            }
        }
        return res;
    };
}

if (!Array.prototype.reduce) {
    Array.prototype.reduce = function reduce(accumulator) {
        if (this === null || this === undefined) throw new TypeError("Object is null or undefined");
        var i = 0,
            l = this.length >> 0,
            curr;
        if (typeof accumulator !== "function") throw new TypeError("First argument is not callable");
        if (arguments.length < 2) {
            if (l === 0) throw new TypeError("Array length is 0 and no second argument");
            curr = this[0];
            i = 1;
        } else curr = arguments[1];
        while (i < l) {
            if (i in this) curr = accumulator.call(undefined, curr, this[i], i, this);
            ++i;
        }
        return curr;
    };
}

if (!Array.prototype.every) {
    Array.prototype.every = function(fun /*, thisp */ ) {
        "use strict";
        if (this == null) throw new TypeError();
        var t = Object(this);
        var len = t.length >>> 0;
        if (typeof fun != "function") throw new TypeError();
        var thisp = arguments[1];
        for (var i = 0; i < len; i++) {
            if (i in t && !fun.call(thisp, t[i], i, t)) return false;
        }
        return true;
    };
}

if (!Array.prototype.some) {
    Array.prototype.some = function(fun /*, thisp */ ) {
        "use strict";
        if (this == null) throw new TypeError();
        var t = Object(this);
        var len = t.length >>> 0;
        if (typeof fun != "function") throw new TypeError();
        var thisp = arguments[1];
        for (var i = 0; i < len; i++) {
            if (i in t && fun.call(thisp, t[i], i, t)) return true;
        }
        return false;
    };
}

if (!Array.prototype.contains) {
    Array.prototype.contains = function(e) {
        "use strict";
        return this.indexOf(e) >= 0;
    };
}

