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
Test random class
import unittest
unittest.TestProgram(argv=["io_test"])
'''
import unittest

import scisoftpy as dnp

TestFolder = "../../../uk.ac.diamond.scisoft.analysis/testfiles/images/"
IOTestFolder = TestFolder + "../gda/analysis/io/"
OutTestFolder = TestFolder + "../../test-scratch/"

import os
isjava = os.name == 'java'

class Test(unittest.TestCase):
    def load(self, name, testfolder=TestFolder):
        path = testfolder + name
        im = dnp.io.load(path)
        print type(im[0]), im[0].shape
        return im

    def colourload(self, name, testfolder=TestFolder):
        path = testfolder + name
        im = dnp.io.load(path, ascolour=True)
        print type(im[0]), im[0].shape
        return im[0]

    def testLoading(self):
        import os
        print os.getcwd()
        self.load("test.png")
        self.load("testrgb.png")

        self.colourload("test.png")
        im = self.colourload("testrgb.png")
        print 'slicing RGB: ', type(im[:5,2])

        self.load("test.jpg")
        self.load("testlossy85.jpg")
        self.load("testrgb.jpg")
        self.load("testrgblossy85.jpg")

        self.colourload("test.jpg")
        self.colourload("testrgb.jpg")
        self.colourload("testrgblossy85.jpg")

        self.load("test.tiff")
        self.load("test-df.tiff")
        self.load("test-pb.tiff")
        self.load("testrgb.tiff")
        self.load("testrgb-df.tiff")
        self.load("testrgb-lzw.tiff")
        self.load("testrgb-pb.tiff")
        try:
            self.load("test-trunc.tiff")
        except IOError, e:
            print 'Expected IO error caught:', e
        except:
            import sys
            print 'Unexpected exception caught', sys.exc_info()

        self.colourload("testrgb.tiff")
        self.colourload("testrgb-df.tiff")
        self.colourload("testrgb-lzw.tiff")
        self.colourload("testrgb-pb.tiff")
        return True

    def testLoadingSRS(self):
        dh = self.load("96356.dat", IOTestFolder+"SRSLoaderTest/")
        print dh.eta

    def testLoadingNXS(self):
        if isjava:
            f = IOTestFolder + "NexusLoaderTest/"
            nm = dnp.io.loadnexus(f + "FeKedge_1_15.nxs")
            print 'There are %d datasets called "Energy"' % len(nm['Energy'])

    def testLoadingHDF(self):
        f = IOTestFolder + "NexusLoaderTest/"
        t = dnp.io.load(f + "FeKedge_1_15.nxs", formats=['hdf5'])
        self.checkTree(t)
        t = dnp.io.load(f + "FeKedge_1_15.nxs")
        self.checkTree(t)

    def checkTree(self, t):
        print t
        g = t['entry1/instrument/FFI0']
        h = g['/entry1/instrument/FFI0']
        self.assertEquals(g, h, "relative and absolute do not match!")
        ga = g['..']
        assert ga is t['entry1/instrument'], "parent is wrong!"
        assert g['.'] is g, "self is wrong!"
        print t['entry1/instrument/FFI0/../../']
        print t['entry1/counterTimer01'].keys()
        l = t.getnodes("Energy")
        print 'List of energy datasets is:', len(l)
        assert len(l) is 1, "Number of energy datasets should be 1"
        d = l[0]
        print type(d)
        assert d.shape == (489,), "Wrong shape"
        dd = d[...]
        assert dd.shape == (489,), "Wrong shape"
        print type(d), type(dd)

    def save(self, name, data, testfolder=OutTestFolder):
        path = testfolder + name
        dnp.io.save(path, data)

    def testSaving(self):
        d = dnp.arange(100).reshape(10,10) % 3
        self.save("chequered.png", d)
        im = self.load("chequered.png", testfolder=OutTestFolder)

    def testB16data(self):
        d = dnp.io.load(IOTestFolder + 'SRSLoaderTest/34146.dat', formats=['srs'])
        print d.keys()
        print d.metadata.keys()
        print d.metadata.values()

    def testNulldata(self):
        try:
            d = self.load("null.dat")
        except Exception, e:
            print "Expected exception caught:", e
        
if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
