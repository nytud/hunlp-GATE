public class TestTest {

    public static void main(String argv[]) {

	System.loadLibrary("test");

	Test t = new Test();
	t.doit("alma");

	longArray x = new longArray(1000);
	int n = t.doit2(x.cast());
	System.out.println(n);
	for (int i=0; i<n; i++) {
		System.out.println(x.getitem(i));
	}

	System.out.println("\n");
	OffsPairArray offs = new OffsPairArray(1000);
	n = t.doit3(offs.cast());
	System.out.println(n);
	for (int i=0; i<10; i++) {
		System.out.format("(%d, %d)\n", offs.getitem(i).getStart(), offs.getitem(i).getEnd());
	}

	System.out.println("\n");
	int[] i = {0};
	t.doit4(i);
	System.out.println(i[0]);


    }
}
