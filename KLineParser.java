import java.util.StringTokenizer;

public class KLineParser {
	public static KLine parse(String line) {
		StringTokenizer toks = new StringTokenizer(line);
	
		KLine k = new KLine();


		k.setType(toks.nextToken().charAt(0));
		k.setMask(toks.nextToken());
		k.setTtl(Integer.parseInt(toks.nextToken()));
		k.setAge(Integer.parseInt(toks.nextToken()));

		String path = toks.nextToken();
		k.setOp(path.substring(0,path.indexOf('!')));
		k.setReason(toks.nextToken());

		return k;
	}

}
