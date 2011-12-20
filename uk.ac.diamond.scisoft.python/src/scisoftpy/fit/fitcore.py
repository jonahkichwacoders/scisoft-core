###
# Copyright © 2011 Diamond Light Source Ltd.
# Contact :  ScientificSoftware@diamond.ac.uk
# 
# This is free software: you can redistribute it and/or modify it under the
# terms of the GNU General Public License version 3 as published by the Free
# Software Foundation.
# 
# This software is distributed in the hope that it will be useful, but 
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
# Public License for more details.
# 
# You should have received a copy of the GNU General Public License along
# with this software. If not, see <http://www.gnu.org/licenses/>.
###

'''

'''

from uk.ac.diamond.scisoft.analysis.fitting.functions import Parameter as _param
from uk.ac.diamond.scisoft.analysis.fitting.functions import AFunction as _absfn
from uk.ac.diamond.scisoft.analysis.fitting.functions import CompositeFunction as _compfn
from uk.ac.diamond.scisoft.analysis.fitting import Fitter as _fitter

import scisoftpy as _dnp
_asIterable = _dnp.asIterable
_toList = _dnp.toList
_asDS = _dnp.asDataset

from scisoftpy.maths import ndarraywrapped as _npwrapped
import java.lang.Class as _jclass #@UnresolvedImport

#from jarray import array as _jarray

#Parameter = _param

import function as _fn

def _createparams(np, params, bounds):
    '''Create a Parameters list with bounds, popping off items from both input lists
    np     -- number of parameters
    params -- list of initial values
    bounds -- list of tuples of bounds
    '''
    params = list(params)
    pl = [ _param(params.pop(0)) for i in range(np) ]

    bounds = list(bounds)
    nbound = len(bounds)
    if nbound > np:
        nbound = np
 
    for i in range(nbound):
        b = bounds.pop(0)
        if b is not None:
            b = _asIterable(b)
            if b[0] is not None:
                pl[i].lowerLimit = b[0]
            if len(b) > 1:
                if b[1] is not None:
                    pl[i].upperLimit = b[1]
#    print [(p.value, p.lowerLimit, p.upperLimit) for p in pl]
    return pl

class fitfunc(_absfn):
    '''Class to wrap an ordinary Jython function for fitting.
    That function should take two arguments:
    p -- list of parameter values
    coords -- coordinates array (or list of such)
    *args -- optional arguments
    '''
    def __init__(self, fn, name, plist, *args):
        '''
        This constructor consumes creates a fit function from given jython function and parameter list

        Arguments:
        fn     -- function
        name   -- function name
        plist  -- list of Parameter objects
        '''
        _absfn.__init__(self, plist) #@UndefinedVariable
        self.func = fn
        self.args = args
        self.name = name

    def val(self, coords):
        '''Evaluate function at single set of coordinates
        '''
        try:
            l = [ p for p in self.parameterValues ]
            l.append(_dnp.array(coords))
            l.append(self.args)
            v = self.func(*l)
            return v.getElementDoubleAbs(0)
        except ValueError:
            raise ValueError, 'Problem with function \"' + self.name + '\" at coord ' + coords + ' with params  ' + self.parameterValues

    @_npwrapped
    def makeDataset(self, coords):
        '''Evaluate function across given coordinates
        '''
        try:
            l = [ p for p in self.parameterValues ]
            l.append([ _dnp.Sciwrap(c) for c in coords])
            l.append(self.args )
            d = self.func(*l)
            d.name = self.name
            return d
        except ValueError:
            raise ValueError, 'Problem with function \"' + self.name + '\" with params  ' + self.parameterValues

    def residual(self, allvalues, data, coords):
        '''Find residual as sum of squared differences of function and data
        
        Arguments:
        allvalues -- boolean, currently ignored 
        data      -- used to subtract from evaluated function
        coords    -- coordinates over which the function is evaluated
        '''
        try:
            l = [ p for p in self.parameterValues ]
            l.append([ _dnp.Sciwrap(c) for c in coords])
            l.append(self.args)
            d = self.func(*l)
            return _dnp.residual(d, data)
        except ValueError:
            raise ValueError, 'Problem with function \"' + self.name + '\" with params  ' + self.parameterValues

class cfitfunc(_compfn):
    '''Composite function for situation where there's a mixture of jython and Java fitting functions
    '''
    def __init__(self):
        _compfn.__init__(self) #@UndefinedVariable

    def val(self, coords):
        '''Evaluate function at single set of coordinates
        '''
        v = 0.
        for n in range(self.noOfFunctions):
            v += self.getFunction(n).val(coords)
        return v

    @_npwrapped
    def makeDataset(self, coords):
        '''Evaluate function across given coordinates
        '''
        vt = None
        for n in range(self.noOfFunctions):
            v = _dnp.Sciwrap(self.getFunction(n).makeDataset(coords))
            if vt is None:
                vt = v
            else:
                vt += v
        return vt

    def residual(self, allvalues, data, coords):
        '''Find residual as sum of squared differences of function and data
        
        Arguments:
        allvalues -- boolean, currently ignored 
        data      -- used to subtract from evaluated function
        coords    -- coordinates over which the function is evaluated
        '''
        return _dnp.residual(self.makeDataset(coords), data)


class fitresult(object):
    '''This is used to contain results from a fit
    '''
    def __init__(self, func, coords, data):
        '''Arguments:
        func   -- function after fitting as occurred
        coords -- coordinate(s)
        data   -- scalar dataset that was fitted to
        '''
        self.func = func
        self.coords = coords
        self.data = data

    def _calcdelta(self, coords):
        delta = 1.
        if coords[0].rank > 1:
            r = coords[0].rank
            for n in range(len(coords)):
                x = coords[n]
                if x.rank != r:
                    raise ValueError, "Given coordinates are not all of same rank"
                delta *= x.ptp()/x.shape[n]
                n += 1
        else:
            for x in coords:
                if x.rank != 1:
                    raise ValueError, "Given coordinates are not all 1D"
                delta *= x.ptp()/x.size
        return delta

    def __getitem__(self, key):
        '''Get specified parameter value
        '''
        try:
            return self.func.getParameterValue(key)
        except:
            raise IndexError

    def __len__(self):
        '''Number of parameters
        '''
        return self.func.getNoOfParameters()

    def makeplotdata(self):
        '''Make a list of datasets to plot
        '''
        pdata = self.makefuncdata()
        pdata.insert(0, self.data)
        offset = self.data.min() - ((self.data.max() - self.data.min()) / 5.0)
        edata = self.data - pdata[1] + offset
        edata.name = "Error value"
        odata = _dnp.zeros_like(edata)
        odata.fill(offset)
        odata.name = "Error offset"
        pdata.insert(2, odata)
        pdata.insert(2, edata)
        return pdata

    def makefuncdata(self):
        '''Make a list of datasets for composite fitting function and its components
        '''
        nf = self.func.noOfFunctions
        if nf > 1:
            fdata = [ self.func.makeDataset(self.coords) ]
            fdata[0].name = "Composite function"
            for n in range(nf):
                fdata.append(self.func.getFunction(n).makeDataset(self.coords))
        elif nf == 1:
            fdata = [ self.func.getFunction(0).makeDataset(self.coords) ]
        else:
            fdata = []

        return fdata


    def plot(self, name=None):
        '''Plot fit as 1D
        '''
        _dnp.plot.line(self.coords[0], self.makeplotdata(), name)

    def _parameters(self):
        '''List of all parameters values
        '''
        return _asDS([ p for p in self.func.getParameterValues() ])
    parameters = property(_parameters)

    def _residual(self):
        '''Residual of fit
        '''
        return self.func.residual(True, self.data, self.coords)
    residual = property(_residual)

    def _area(self):
        '''Area or hypervolume under fit assuming coordinates are uniformly spaced
        '''
        deltax = self._calcdelta(self.coords)
        return self.func.makeDataset(self.coords).sum() * deltax
    area = property(_area)

    def __str__(self):
        nf = self.func.noOfFunctions
        out = "Fit parameters:\n"
        for n in range(nf):
            f = self.func.getFunction(n)
            p = [ q for q in f.getParameterValues() ]
            np = len(p)
            out += "    function '%s' (%d) has %d parameters = %s\n" % (f.name, n, np, p)
        return out

import inspect as _inspect

def fit(func, coords, data, p0, bounds=[], args=None, ptol=1e-4, seed=None, optimizer="local"):
    '''
    Arguments:
    func      -- function (or list of functions)
    coords    -- coordinate dataset(s)
    data      -- data to fit
    p0        -- list of initial parameters
    bounds    -- list of parameter bounds, bounds are tuples of lower and upper values (any can be None)
    args      -- extra arguments
    ptol      -- parameter fit tolerance
    seed      -- seed value for genetic algorithm-based optimiser
    optimizer -- description of the optimizer to use, e.g. ['local','global','simplex','genetic',
                 'gradient','apache_nm','apache_md','apache_cg']
                 local and global are general settings, which point the one of the specific methods
                 If any global methods are picked, the bounds argument must aslo be filled in.
    Returns:
    fitresult object
    '''
    fnlist = []
    if not isinstance(func, list):
        func = [func]
    if not isinstance(p0, list):
        p0 = [p0]
    if not isinstance(bounds, list):
        bounds = [bounds]
    mixed = False
    for f in _toList(func):
        if isinstance(f, tuple):
            print 'parameter count is no longer required'
            f = f[0]
        if isinstance(f, _jclass):
            # create bound function object
            np = _fn.nparams(f)
            pl = _createparams(np, p0, bounds)
            fnlist.append(f(pl))
        elif not _inspect.isfunction(f):
            # instantiated Java function
            np = f.getNoOfParameters()
            fnlist.append(f)
        else:
            np = len(_inspect.getargspec(f)[0]) - 1
            if np < 1:
                raise ValueError, "Function needs more than one argument (i.e. at least one parameter)"
            pl = _createparams(np, p0, bounds)
            fnlist.append(fitfunc(f, f.__name__, pl, args))
            mixed = True

    if not mixed: # no jython functions
        cfunc = _compfn()
    else:
        cfunc = cfitfunc()
    for f in fnlist:
        cfunc.addFunction(f)

    coords = list(_asIterable(coords))
    for i in range(len(coords)): # check and slice coordinates to match data
        c = coords[i]
        if c.shape != data.shape:
            ns = [ slice(d) for d in data.shape ]
            coords[i] = c[ns]

    if seed:
        _fitter.seed = int(seed)

    import time

    start = -time.time()
    
    # use the appropriate fitter for the task
    if optimizer == 'local' :
        _fitter.simplexFit(ptol, coords, data, cfunc)
    elif optimizer == 'global' :
        if len(bounds) == 0 :
            print "Using a global optimizer with no bounds is unlikely to work, please use the bounds argument to narrow the search space" 
        _fitter.geneticFit(ptol, coords, data, cfunc)
    elif optimizer == 'simplex' :
        _fitter.simplexFit(ptol, coords, data, cfunc)
    elif optimizer == 'gradient' :
        _fitter.GDFit(ptol, coords, data, cfunc)
    elif optimizer == 'apache_nm' :
        _fitter.ApacheNelderMeadFit(coords, data, cfunc)
    elif optimizer == 'apache_md' :
        _fitter.ApacheMultiDirectionFit(coords, data, cfunc)
    elif optimizer == 'apache_cg' :
        _fitter.ApacheConjugateGradientFit(coords, data, cfunc)
    elif optimizer == 'genetic' :
        if len(bounds) == 0 :
            print "Using a global optimizer with no bounds is unlikely to work, please use the bounds argument to narrow the search space" 
        _fitter.geneticFit(ptol, coords, data, cfunc)
        
    start += time.time()
    print "Fit took %fs" % start

    return fitresult(cfunc, coords, data)

# genfit = _genfit

def _polycoeff(roots):
    '''Calculate polynomial coefficients from roots'''
    nr = len(roots)
    oc = [1.0]
    for n in range(nr):
        r = -roots[n]
        nc = [ r*c for c in oc ]
        for m in range(n):
            oc[m+1] += nc[m]
        oc.append(nc[n])
    return oc

from uk.ac.diamond.scisoft.analysis.fitting.functions import Polynomial as _poly

class poly1d(object):
    '''1D polynomial class'''
    def __init__(self, c_or_r, r=0, variable=None):
        if variable:
            self.variable = variable
        else:
            self.variable = 'x'
        if r:
            self.order = len(c_or_r)
            # expansion of factors
            self.c = _polycoeff(c_or_r)
            self.roots = c_or_r
        else:
            self.order = len(c_or_r) - 1
            self.c = c_or_r
            self.roots = None

    def __str__(self):
        par  = self.c
        if self.order > 1:
            base = str(par[0]) + ' ' + self.variable
            spow = str(self.order)
            sup  = ' '*len(base) + spow
            base += ' '*len(spow)
        elif self.order == 1:
            base = str(par[0]) + ' ' + self.variable
            sup  = ' '*len(base)
        else:
            base = str(par[0])
            sup  = ' '*len(base)
            
        for i in range(1, self.order + 1):
            p = par[i]
            if p > 0:
                term = ' + ' + str(p)
            elif p < 0:
                term = ' - ' + str(-p)
            else:
                continue

            lpow = self.order - i
            if lpow > 1:
                term += ' ' + self.variable
                spow = str(lpow)
                sup  += ' '*len(term) + spow
                base += term + ' '*len(spow)
            elif lpow == 1:
                term += ' ' + self.variable
                sup  += ' '*len(term)
                base += term
            else:
                sup  += ' '*len(term)
                base += term

        return sup + '\n' + base + '\n'

    def __getitem__(self, k):
        return self.c[-(k+1)]

    def _getroots(self):
        if self.roots:
            return self.roots
        raise NotImplementedError # implement Bairstow's method
    r = property(_getroots)

def polyfit(x, y, deg, rcond=None, full=False):
    '''Linear least squares polynomial fit

    Fit a polynomial p(x) = p[0] * x**deg + ... + p[deg] of order deg to points (x, y).
    Arguments:
    x   -- x-coordinates of sample points
    y   -- y-coordinates of sample points
    deg -- order of fitting polynomial
    
    Returns:
    a vector of coefficients p that minimises the squared error and
    a fitresult object
    '''
    poly = _poly(deg)
    x = _asIterable(x)
    if rcond is None:
        rcond = 2e-16*y.size
    _fitter.polyFit(x, y, rcond, poly)
    fr = fitresult(poly, x, y)
    if full:
        return fr.parameters, fr
    else:
        return fr.parameters

@_npwrapped
def polyval(p, x):
    '''Evaluate polynomial at given points
    If p is of length N, this function returns the value:
    p[0]*(x**N-1) + p[1]*(x**N-2) + ... + p[N-2]*x + p[N-1]
    '''
    poly = _poly(_asDS(p, _dnp.float64).data)
    d = _asDS(x, _dnp.float, force=True)
    return poly.makeDataset([d])

# need a cspline fit function

from uk.ac.diamond.scisoft.analysis.fitting import EllipseFitter as _efitter

def ellipsefit(x, y, geo=True, init=None):
    '''Ellipse fit
    
    Fit an ellipse to a set of points (in 2D).
    Arguments:
    x   -- x-coordinates of sample points
    y   -- y-coordinates of sample points
    geo -- flag to use for geometric (slow) or algebraic (fast) fitting
    init -- initial parameters

    Returns:
    a vector of geometric parameters (major, minor semi-axes, major axis angle, centre coordinates)
    '''
    f = _efitter() #.EllipseFitter()
    if geo:
        f.geometricFit(_asDS(x), _asDS(y), init)
    else:
        f.algebraicFit(_asDS(x), _asDS(y))

    return f.parameters


@_npwrapped
def makeellipse(p, t=None):
    '''Generate two datasets containing coordinates for points on an ellipse

    Arguments:
    p -- geometric parameters (major, minor semi-axes, major axis angle, centre coordinates)
    t -- array of angles (can be None for a 100-point array spanning 2 pi)
    '''
    if t is None:
        t = _dnp.arange(100)*_dnp.pi/50.

    return _efitter.generateCoordinates(_asDS(t), p)


