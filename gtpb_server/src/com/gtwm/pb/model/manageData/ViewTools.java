package com.gtwm.pb.model.manageData;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.MathContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.ViewToolsInfo;
import com.gtwm.pb.model.interfaces.FilterTypeDescriptorInfo;
import com.gtwm.pb.model.manageSchema.FilterTypeDescriptor;
import com.gtwm.pb.util.Enumerations.FilterType;
import com.gtwm.pb.util.Enumerations.Browsers;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.interfaces.fields.TextValue;
import com.gtwm.pb.model.interfaces.fields.FileValue;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageData.fields.TextValueDefn;
import com.gtwm.pb.model.manageData.fields.FileValueDefn;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.AppProperties;
import com.gtwm.pb.util.RandomString;

import org.apache.velocity.tools.generic.MathTool;
import org.grlea.log.SimpleLogger;
import com.ibm.icu.text.RuleBasedNumberFormat;

public class ViewTools implements ViewToolsInfo {

	private ViewTools() {
	}

	public ViewTools(HttpServletRequest request, HttpServletResponse response, String webAppRoot) {
		this.request = request;
		this.response = response;
		this.webAppRoot = webAppRoot;
	}

	public String getWebAppRoot() {
		return this.webAppRoot;
	}

	public String spelloutDecimal(double number) {
		RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(RuleBasedNumberFormat.SPELLOUT);
		return rbnf.format(number);
	}

	public String spelloutCurrencyFromString(String number) {
		double doubleNumber = Double.valueOf(number);
		double poundsPart = Math.floor(doubleNumber);
		String penceString = "0";
		if (number.contains(".")) {
			penceString = number.replaceAll("^.*\\.", "");
		}
		int pencePart = Integer.valueOf(penceString);
		return this.spelloutDecimal(poundsPart) + " pounds " + pencePart + "p";
	}

	public String getDatestampString() {
		Calendar calendar = Calendar.getInstance();
		return String.format("%1$td/%1$tm/%1$tY", calendar);
	}

	public Calendar getCalendar() {
		return Calendar.getInstance();
	}

	public int getCalendarConstant(String constantName) throws CantDoThatException {
		if (constantName.equalsIgnoreCase("YEAR")) {
			return Calendar.YEAR;
		}
		if (constantName.equalsIgnoreCase("DAY_OF_MONTH")) {
			return Calendar.DAY_OF_MONTH;
		}
		if (constantName.equalsIgnoreCase("MONTH")) {
			return Calendar.MONTH;
		}
		if (constantName.equalsIgnoreCase("HOUR_OF_DAY")) {
			return Calendar.HOUR_OF_DAY;
		}
		if (constantName.equalsIgnoreCase("MINUTE")) {
			return Calendar.MINUTE;
		}
		if (constantName.equalsIgnoreCase("SECOND")) {
			return Calendar.SECOND;
		}
		throw new CantDoThatException("Unimplemented calendar constant: " + constantName);
	}

	public String getAreaForPhoneNumber(String phoneNumber) {
		if (this.areaCodes.size() == 0) {
			// initialise array, values from wikipedia
			this.areaCodes.put("020", "London");
			this.areaCodes.put("0121", "Birmingham");
			this.areaCodes.put("023", "Southampton or Portsmouth");
			this.areaCodes.put("0113", "Leeds");
			this.areaCodes.put("0131", "Edinburgh");
			this.areaCodes.put("024", "Coventry");
			this.areaCodes.put("0114", "Sheffield");
			this.areaCodes.put("0141", "Glasgow");
			this.areaCodes.put("0115", "Nottingham");
			this.areaCodes.put("0151", "Liverpool");
			this.areaCodes.put("0116", "Leicester");
			this.areaCodes.put("0161", "Manchester");
			this.areaCodes.put("0117", "Bristol");
			this.areaCodes.put("028", "Northern Ireland");
			this.areaCodes.put("0118", "Reading");
			this.areaCodes.put("029", "Cardiff");
			this.areaCodes.put("0191", "Tyne and Wear or Durham");
			this.areaCodes.put("01200", "Clitheroe");
			this.areaCodes.put("01202", "Bournemouth");
			this.areaCodes.put("01204", "Bolton");
			this.areaCodes.put("01205", "Boston");
			this.areaCodes.put("01206", "Colchester");
			this.areaCodes.put("01207", "Consett");
			this.areaCodes.put("01208", "Bodmin");
			this.areaCodes.put("01209", "Redruth, Cornwall");
			this.areaCodes.put("01223", "Cambridge");
			this.areaCodes.put("01224", "Aberdeen");
			this.areaCodes.put("01225", "Bath");
			this.areaCodes.put("01226", "Barnsley");
			this.areaCodes.put("01227", "Canterbury");
			this.areaCodes.put("01228", "Carlisle");
			this.areaCodes.put("01229", "Barrow-in-Furness");
			this.areaCodes.put("01233", "Ashford");
			this.areaCodes.put("01234", "Bedford");
			this.areaCodes.put("01235", "Abingdon");
			this.areaCodes.put("01236", "Coatbridge");
			this.areaCodes.put("01237", "Bideford");
			this.areaCodes.put("01239", "Cardigan");
			this.areaCodes.put("01241", "Arbroath");
			this.areaCodes.put("01242", "Cheltenham");
			this.areaCodes.put("01243", "Chichester, West Sussex");
			this.areaCodes.put("01244", "Chester");
			this.areaCodes.put("01245", "Chelmsford");
			this.areaCodes.put("01246", "Chesterfield");
			this.areaCodes.put("01248", "Bangor, Wales");
			this.areaCodes.put("01249", "Chippenham");
			this.areaCodes.put("01250", "Blairgowrie");
			this.areaCodes.put("01252", "Aldershot");
			this.areaCodes.put("01253", "Blackpool");
			this.areaCodes.put("01254", "Blackburn");
			this.areaCodes.put("01255", "Clacton on Sea");
			this.areaCodes.put("01256", "Basingstoke");
			this.areaCodes.put("01257", "Chorley");
			this.areaCodes.put("01258", "Blandford");
			this.areaCodes.put("01259", "Alloa");
			this.areaCodes.put("01260", "Congleton");
			this.areaCodes.put("01261", "Banff");
			this.areaCodes.put("01262", "Bridlington");
			this.areaCodes.put("01263", "Cromer");
			this.areaCodes.put("01264", "Andover");
			this.areaCodes.put("01267", "Carmarthen");
			this.areaCodes.put("01268", "Basildon");
			this.areaCodes.put("01269", "Ammanford");
			this.areaCodes.put("01270", "Crewe");
			this.areaCodes.put("01271", "Barnstaple");
			this.areaCodes.put("01273", "Brighton");
			this.areaCodes.put("01274", "Bradford");
			this.areaCodes.put("01275", "Clevedon, Bristol");
			this.areaCodes.put("01276", "Camberley");
			this.areaCodes.put("01277", "Brentwood");
			this.areaCodes.put("01278", "Bridgwater");
			this.areaCodes.put("01279", "Bishop's Stortford");
			this.areaCodes.put("01280", "Buckingham");
			this.areaCodes.put("01282", "Burnley, BU");
			this.areaCodes.put("01283", "Burton upon Trent");
			this.areaCodes.put("01284", "Bury St. Edmunds");
			this.areaCodes.put("01285", "Cirencester");
			this.areaCodes.put("01286", "Caernarfon");
			this.areaCodes.put("01287", "Guisborough");
			this.areaCodes.put("01288", "Bude");
			this.areaCodes.put("01289", "Berwick on Tweed");
			this.areaCodes.put("01290", "Cumnock, Ayrshire");
			this.areaCodes.put("01291", "Chepstow");
			this.areaCodes.put("01292", "Ayr");
			this.areaCodes.put("01293", "Crawley");
			this.areaCodes.put("01294", "Ardrossan, Ayrshire");
			this.areaCodes.put("01295", "Banbury");
			this.areaCodes.put("01296", "Aylesbury");
			this.areaCodes.put("01297", "Axminster");
			this.areaCodes.put("01298", "Buxton");
			this.areaCodes.put("01299", "Bewdley");
			this.areaCodes.put("01300", "Cerne Abbas, Dorset");
			this.areaCodes.put("01301", "Arrochar");
			this.areaCodes.put("01302", "Doncaster");
			this.areaCodes.put("01303", "Folkestone");
			this.areaCodes.put("01304", "Dover");
			this.areaCodes.put("01305", "Dorchester");
			this.areaCodes.put("01306", "Dorking");
			this.areaCodes.put("01307", "Forfar");
			this.areaCodes.put("01308", "Bridport, Dorset");
			this.areaCodes.put("01309", "Forres");
			this.areaCodes.put("01320", "Fort Augustus");
			this.areaCodes.put("01322", "Dartford");
			this.areaCodes.put("01323", "Eastbourne, EA");
			this.areaCodes.put("01324", "Falkirk, FA");
			this.areaCodes.put("01325", "Darlington");
			this.areaCodes.put("01326", "Falmouth");
			this.areaCodes.put("01327", "Daventry");
			this.areaCodes.put("01328", "Fakenham");
			this.areaCodes.put("01329", "Fareham");
			this.areaCodes.put("01330", "Banchory, Deeside");
			this.areaCodes.put("01332", "Derby");
			this.areaCodes.put("01333", "Peat Inn, Fife");
			this.areaCodes.put("01334", "St Andrews, Fife");
			this.areaCodes.put("01335", "Ashbourne");
			this.areaCodes.put("01337", "Ladybank, Fife");
			this.areaCodes.put("01340", "Craigellachie, Elgin");
			this.areaCodes.put("01341", "Barmouth, Dolgellau");
			this.areaCodes.put("01342", "East Grinstead");
			this.areaCodes.put("01343", "Elgin");
			this.areaCodes.put("01344", "Bracknell, Easthampstead");
			this.areaCodes.put("01346", "Fraserburgh");
			this.areaCodes.put("01347", "Easingwold");
			this.areaCodes.put("01348", "Fishguard");
			this.areaCodes.put("01349", "Dingwall");
			this.areaCodes.put("01350", "Dunkeld");
			this.areaCodes.put("01352", "Mold, Flint");
			this.areaCodes.put("01353", "Ely");
			this.areaCodes.put("01354", "March, Cambridgeshire, Fenland");
			this.areaCodes.put("01355", "East Kilbride");
			this.areaCodes.put("01356", "Brechin, Edzell");
			this.areaCodes.put("01357", "Strathaven, East Kilbride");
			this.areaCodes.put("01358", "Ellon");
			this.areaCodes.put("01359", "Pakenham, Elmswell");
			this.areaCodes.put("01360", "Killearn, Drymen");
			this.areaCodes.put("01361", "Duns");
			this.areaCodes.put("01362", "Dereham");
			this.areaCodes.put("01363", "Crediton");
			this.areaCodes.put("01364", "Ashburton, Devon");
			this.areaCodes.put("01366", "Downham Market");
			this.areaCodes.put("01367", "Faringdon");
			this.areaCodes.put("01368", "Dunbar");
			this.areaCodes.put("01369", "Dunoon");
			this.areaCodes.put("01371", "Great Dunmow, Essex");
			this.areaCodes.put("01372", "Epsom");
			this.areaCodes.put("01373", "Frome");
			this.areaCodes.put("01375", "Grays Thurrock, Essex");
			this.areaCodes.put("01376", "Braintree, Essex");
			this.areaCodes.put("01377", "Driffield");
			this.areaCodes.put("01379", "Diss");
			this.areaCodes.put("01380", "Devizes");
			this.areaCodes.put("01381", "Fortrose");
			this.areaCodes.put("01382", "Dundee");
			this.areaCodes.put("01383", "Dunfermline");
			this.areaCodes.put("01384", "Dudley");
			this.areaCodes.put("01386", "Evesham");
			this.areaCodes.put("01387", "Dumfries");
			this.areaCodes.put("013873", "Langholm");
			this.areaCodes.put("01388", "Bishop Auckland, Durham");
			this.areaCodes.put("01389", "Dumbarton");
			this.areaCodes.put("01392", "Exeter");
			this.areaCodes.put("01394", "Felixstowe");
			this.areaCodes.put("01395", "Budleigh Salterton, Exmouth");
			this.areaCodes.put("01397", "Fort William");
			this.areaCodes.put("01398", "Dulverton, Exmoor");
			this.areaCodes.put("01400", "Honington");
			this.areaCodes.put("01403", "Horsham");
			this.areaCodes.put("01404", "Honiton");
			this.areaCodes.put("01405", "Goole");
			this.areaCodes.put("01406", "Holbeach");
			this.areaCodes.put("01407", "Holyhead");
			this.areaCodes.put("01408", "Golspie");
			this.areaCodes.put("01409", "Holsworthy");
			this.areaCodes.put("01420", "Alton");
			this.areaCodes.put("01422", "Halifax");
			this.areaCodes.put("01423", "Boroughbridge and Harrogate");
			this.areaCodes.put("01424", "Hastings");
			this.areaCodes.put("01425", "Ringwood, Highcliffe; New Milton, Ashley");
			this.areaCodes.put("01427", "Gainsborough");
			this.areaCodes.put("01428", "Haslemere");
			this.areaCodes.put("01429", "Hartlepool");
			this.areaCodes.put("01430", "Market Weighton and North Cave, Howden");
			this.areaCodes.put("01431", "Helmsdale");
			this.areaCodes.put("01432", "Hereford");
			this.areaCodes.put("01433", "Hathersage");
			this.areaCodes.put("01434", "Bellingham, Haltwhistle and Hexham");
			this.areaCodes.put("01435", "Heathfield");
			this.areaCodes.put("01436", "Helensburgh");
			this.areaCodes.put("01437", "Clynderwen and Haverfordwest");
			this.areaCodes.put("01438", "Stevenage, Hertfordshire");
			this.areaCodes.put("01439", "Helmsley");
			this.areaCodes.put("01440", "Haverhill");
			this.areaCodes.put("01442", "Hemel Hempstead -HH");
			this.areaCodes.put("01443", "Pontypridd, Glamorgan");
			this.areaCodes.put("01444", "Haywards Heath");
			this.areaCodes.put("01445", "Gairloch");
			this.areaCodes.put("01446", "Barry, Glamorgan");
			this.areaCodes.put("01449", "Stowmarket, Gipping");
			this.areaCodes.put("01450", "Hawick");
			this.areaCodes.put("01451", "Stow-on-the-Wold, Gloucestershire");
			this.areaCodes.put("01452", "Gloucester");
			this.areaCodes.put("01453", "Dursley, Gloucestershire");
			this.areaCodes.put("01454", "Chipping Sodbury, Gloucestershire");
			this.areaCodes.put("01455", "Hinckley");
			this.areaCodes.put("01456", "Glenurquhart");
			this.areaCodes.put("01457", "Glossop");
			this.areaCodes.put("01458", "Glastonbury");
			this.areaCodes.put("01460", "Chard, Ilminster");
			this.areaCodes.put("01461", "Gretna");
			this.areaCodes.put("01462", "Hitchin");
			this.areaCodes.put("01463", "Inverness");
			this.areaCodes.put("01464", "Insch");
			this.areaCodes.put("01465", "Girvan");
			this.areaCodes.put("01466", "Huntly");
			this.areaCodes.put("01467", "Inverurie");
			this.areaCodes.put("01469", "Killingholme, Immingham");
			this.areaCodes.put("01470", "Edinbane, Isle of Skye");
			this.areaCodes.put("01471", "Broadford, Isle of Skye");
			this.areaCodes.put("01472", "Grimsby");
			this.areaCodes.put("01473", "Ipswich");
			this.areaCodes.put("01474", "Gravesend");
			this.areaCodes.put("01475", "Greenock");
			this.areaCodes.put("01476", "Grantham");
			this.areaCodes.put("01477", "Holmes Chapel");
			this.areaCodes.put("01478", "Portree, Isle of Skye");
			this.areaCodes.put("01479", "Grantown on Spey");
			this.areaCodes.put("01480", "Huntingdon");
			this.areaCodes.put("01481", "Guernsey");
			this.areaCodes.put("01482", "Hull");
			this.areaCodes.put("01483", "Guildford");
			this.areaCodes.put("01484", "Huddersfield");
			this.areaCodes.put("01485", "Hunstanton");
			this.areaCodes.put("01487", "Warboys, Huntingdon");
			this.areaCodes.put("01488", "Hungerford");
			this.areaCodes.put("01489", "Bishops Waltham, Hamble Valley");
			this.areaCodes.put("01490", "Corwen, Gwynedd");
			this.areaCodes.put("01491", "Henley-on-Thames");
			this.areaCodes.put("01492", "Colwyn Bay, Gwynedd");
			this.areaCodes.put("01493", "Great Yarmouth");
			this.areaCodes.put("01494", "High Wycombe");
			this.areaCodes.put("01495", "Pontypool, Gwent");
			this.areaCodes.put("01496", "Port Ellen, Islay");
			this.areaCodes.put("01497", "Hay on Wye");
			this.areaCodes.put("01499", "Inveraray");
			this.areaCodes.put("01501", "Harthill, Lothian");
			this.areaCodes.put("01502", "Lowestoft");
			this.areaCodes.put("01503", "Looe");
			this.areaCodes.put("01505", "Johnstone");
			this.areaCodes.put("01506", "Bathgate, Lothian");
			this.areaCodes.put("01507", "Alford, Louth and Spilsby");
			this.areaCodes.put("01508", "Brooke");
			this.areaCodes.put("01509", "Loughborough");
			this.areaCodes.put("01520", "Lochcarron");
			this.areaCodes.put("01522", "Lincoln");
			this.areaCodes.put("01524", "Lancaster");
			this.areaCodes.put("015242", "Hornby");
			this.areaCodes.put("01525", "Leighton Buzzard");
			this.areaCodes.put("01526", "Martin, Lincolnshire");
			this.areaCodes.put("01527", "Redditch");
			this.areaCodes.put("01528", "Laggan, Badenoch");
			this.areaCodes.put("01529", "Sleaford, Lincolnshire");
			this.areaCodes.put("01530", "Coalville, Ashby-de-la-Zouch Leicestershire");
			this.areaCodes.put("01531", "Ledbury");
			this.areaCodes.put("01534", "Jersey");
			this.areaCodes.put("01535", "Keighley");
			this.areaCodes.put("01536", "Kettering");
			this.areaCodes.put("01538", "Ipstones, Leek");
			this.areaCodes.put("01539", "Kendal");
			this.areaCodes.put("015394", "Hawkshead");
			this.areaCodes.put("015395", "Grange over Sands");
			this.areaCodes.put("015396", "Sedbergh");
			this.areaCodes.put("01540", "Kingussie");
			this.areaCodes.put("01542", "Keith");
			this.areaCodes.put("01543", "Cannock, Lichfield");
			this.areaCodes.put("01544", "Kington");
			this.areaCodes.put("01545", "Llanarth, Ceredigion");
			this.areaCodes.put("01546", "Lochgilphead");
			this.areaCodes.put("01547", "Knighton");
			this.areaCodes.put("01548", "Kingsbridge");
			this.areaCodes.put("01549", "Lairg");
			this.areaCodes.put("01550", "Llandovery");
			this.areaCodes.put("01553", "King's Lynn");
			this.areaCodes.put("01554", "Llanelli");
			this.areaCodes.put("01555", "Lanark");
			this.areaCodes.put("01556", "Castle Douglas, Kirkcudbrightshire");
			this.areaCodes.put("01557", "Kirkcudbright");
			this.areaCodes.put("01558", "Llandeilo");
			this.areaCodes.put("01559", "Llandysul");
			this.areaCodes.put("01560", "Moscow");
			this.areaCodes.put("01561", "Laurencekirk");
			this.areaCodes.put("01562", "Kidderminster");
			this.areaCodes.put("01563", "Kilmarnock");
			this.areaCodes.put("01564", "Lapworth, Knowle");
			this.areaCodes.put("01565", "Knutsford");
			this.areaCodes.put("01566", "Launceston");
			this.areaCodes.put("01567", "Killin");
			this.areaCodes.put("01568", "Leominster");
			this.areaCodes.put("01569", "Stonehaven, Laurencekirk");
			this.areaCodes.put("01570", "Lampeter");
			this.areaCodes.put("01571", "Lochinver");
			this.areaCodes.put("01572", "Oakham");
			this.areaCodes.put("01573", "Kelso");
			this.areaCodes.put("01575", "Kirriemuir");
			this.areaCodes.put("01576", "Lockerbie");
			this.areaCodes.put("01577", "Kinross");
			this.areaCodes.put("01578", "Lauder");
			this.areaCodes.put("01579", "Liskeard");
			this.areaCodes.put("01580", "Cranbrook, Kent");
			this.areaCodes.put("01581", "New Luce, Luce");
			this.areaCodes.put("01582", "Luton");
			this.areaCodes.put("01583", "Carradale, Kintyre");
			this.areaCodes.put("01584", "Ludlow");
			this.areaCodes.put("01586", "Campbeltown, Kintyre");
			this.areaCodes.put("01588", "Bishops Castle, Ludlow");
			this.areaCodes.put("01590", "Lymington");
			this.areaCodes.put("01591", "Llanwrtyd Wells");
			this.areaCodes.put("01592", "Kirkcaldy");
			this.areaCodes.put("01593", "Lybster");
			this.areaCodes.put("01594", "Lydney");
			this.areaCodes.put("01595", "Lerwick, Foula and Fair Isle");
			this.areaCodes.put("01597", "Llandrindod Wells");
			this.areaCodes.put("01598", "Lynton");
			this.areaCodes.put("01599", "Kyle");
			this.areaCodes.put("01600", "Monmouth");
			this.areaCodes.put("01603", "Norwich");
			this.areaCodes.put("01604", "Northampton");
			this.areaCodes.put("01606", "Northwich");
			this.areaCodes.put("01608", "Chipping Norton");
			this.areaCodes.put("01609", "Northallerton");
			this.areaCodes.put("01620", "North Berwick");
			this.areaCodes.put("01621", "Maldon");
			this.areaCodes.put("01622", "Maidstone");
			this.areaCodes.put("01623", "Mansfield");
			this.areaCodes.put("01624", "Isle of Man");
			this.areaCodes.put("01625", "Macclesfield");
			this.areaCodes.put("01626", "Newton Abbot");
			this.areaCodes.put("01628", "Maidenhead");
			this.areaCodes.put("01629", "Matlock");
			this.areaCodes.put("01630", "Market Drayton");
			this.areaCodes.put("01631", "Oban");
			this.areaCodes.put("01632",
					"numbers are now used as fictional numbers for drama purposes");
			this.areaCodes.put("01633", "Newport");
			this.areaCodes.put("01634", "Medway");
			this.areaCodes.put("01635", "Newbury");
			this.areaCodes.put("01636", "Newark-on-Trent");
			this.areaCodes.put("01637", "Newquay");
			this.areaCodes.put("01638", "Newmarket");
			this.areaCodes.put("01639", "Neath");
			this.areaCodes.put("01641", "Strathy, Melvich");
			this.areaCodes.put("01642", "Middlesbrough");
			this.areaCodes.put("01643", "Minehead");
			this.areaCodes.put("01644", "New Galloway");
			this.areaCodes.put("01646", "Milford Haven");
			this.areaCodes.put("01647", "Moretonhampstead");
			this.areaCodes.put("01650", "Cemmaes Road, Machynlleth");
			this.areaCodes.put("01651", "Oldmeldrum");
			this.areaCodes.put("01652", "Brigg, North Kelsey");
			this.areaCodes.put("01653", "Malton");
			this.areaCodes.put("01654", "Machynlleth");
			this.areaCodes.put("01655", "Maybole");
			this.areaCodes.put("01656", "Bridgend");
			this.areaCodes.put("01659", "Sanquhar, Nithsdale");
			this.areaCodes.put("01661", "Prudhoe, Northumberland");
			this.areaCodes.put("01663", "New Mills");
			this.areaCodes.put("01664", "Melton Mowbray");
			this.areaCodes.put("01665", "Alnwick, Northumberland");
			this.areaCodes.put("01666", "Malmesbury");
			this.areaCodes.put("01667", "Nairn");
			this.areaCodes.put("01668", "Bamburgh, Northumberland");
			this.areaCodes.put("01669", "Rothbury, Northumberland");
			this.areaCodes.put("01670", "Morpeth");
			this.areaCodes.put("01671", "Newton Stewart");
			this.areaCodes.put("01672", "Marlborough");
			this.areaCodes.put("01673", "Market Rasen");
			this.areaCodes.put("01674", "Montrose");
			this.areaCodes.put("01675", "Coleshill, Warwickshire, Meriden");
			this.areaCodes.put("01676", "Meriden");
			this.areaCodes.put("01677", "Bedale, North Riding");
			this.areaCodes.put("01678", "Bala, Meirionydd");
			this.areaCodes.put("01680", "Craignure, Isle of Mull");
			this.areaCodes.put("01681", "Fionnphort, Isle of Mull");
			this.areaCodes.put("01683", "Moffat");
			this.areaCodes.put("01684", "Malvern");
			this.areaCodes.put("01685", "Merthyr Tydfil");
			this.areaCodes.put("01686", "Llanidloes and Newtown");
			this.areaCodes.put("01687", "Mallaig");
			this.areaCodes.put("01688", "Tobermory, Isle of Mull");
			this.areaCodes.put("01689", "Orpington");
			this.areaCodes.put("01690", "Betws-y-Coed");
			this.areaCodes.put("01692", "North Walsham");
			this.areaCodes.put("01694", "Church Stretton");
			this.areaCodes.put("01697", "Brampton");
			this.areaCodes.put("016973", "Wigton");
			this.areaCodes.put("016974", "Raughton Head");
			this.areaCodes.put("016977", "Hallbankgate");
			this.areaCodes.put("01698", "Motherwell");
			this.areaCodes.put("01700", "Rothesay");
			this.areaCodes.put("01702", "Southend on Sea");
			this.areaCodes.put("01704", "Southport");
			this.areaCodes.put("01706", "Rochdale, Rossendale");
			this.areaCodes.put("01707", "Welwyn Hatfield and Potters Bar");
			this.areaCodes.put("01708", "Romford");
			this.areaCodes.put("01709", "Rotherham");
			this.areaCodes.put("01720", "Isles of Scilly");
			this.areaCodes.put("01721", "Peebles");
			this.areaCodes.put("01722", "Salisbury");
			this.areaCodes.put("01723", "Scarborough");
			this.areaCodes.put("01724", "Scunthorpe");
			this.areaCodes.put("01725", "Rockbourne");
			this.areaCodes.put("01726", "St Austell");
			this.areaCodes.put("01727", "St Albans");
			this.areaCodes.put("01728", "Saxmundham");
			this.areaCodes.put("01729", "Settle, Ribblesdale");
			this.areaCodes.put("01730", "Petersfield");
			this.areaCodes.put("01732", "Sevenoaks");
			this.areaCodes.put("01733", "Peterborough");
			this.areaCodes.put("01736", "Penzance");
			this.areaCodes.put("01737", "Redhill");
			this.areaCodes.put("01738", "Perth");
			this.areaCodes.put("01740", "Sedgefield");
			this.areaCodes.put("01743", "Shrewsbury");
			this.areaCodes.put("01744", "St Helens");
			this.areaCodes.put("01745", "Rhyl");
			this.areaCodes.put("01746", "Bridgnorth, Shropshire");
			this.areaCodes.put("01747", "Shaftesbury");
			this.areaCodes.put("01748", "Richmond");
			this.areaCodes.put("01749", "Shepton Mallet");
			this.areaCodes.put("01750", "Selkirk");
			this.areaCodes.put("01751", "Pickering");
			this.areaCodes.put("01752", "Plymouth");
			this.areaCodes.put("01753", "Slough");
			this.areaCodes.put("01754", "Skegness");
			this.areaCodes.put("01756", "Skipton");
			this.areaCodes.put("01757", "Selby");
			this.areaCodes.put("01758", "Pwllheli");
			this.areaCodes.put("01759", "Pocklington");
			this.areaCodes.put("01760", "Swaffham");
			this.areaCodes.put("01761", "Temple Cloud, Somerset");
			this.areaCodes.put("01763", "Royston");
			this.areaCodes.put("01764", "Crieff, Ruthven");
			this.areaCodes.put("01765", "Ripon");
			this.areaCodes.put("01766", "Porthmadog");
			this.areaCodes.put("01767", "Sandy");
			this.areaCodes.put("01768", "Penrith");
			this.areaCodes.put("017683", "Appleby");
			this.areaCodes.put("017684", "Pooley Bridge");
			this.areaCodes.put("017687", "Keswick");
			this.areaCodes.put("01769", "South Molton");
			this.areaCodes.put("01770", "Isle of Arran");
			this.areaCodes.put("01771", "Maud");
			this.areaCodes.put("01772", "Preston");
			this.areaCodes.put("01773", "Ripley");
			this.areaCodes.put("01775", "Spalding");
			this.areaCodes.put("01776", "Stranraer");
			this.areaCodes.put("01777", "Retford");
			this.areaCodes.put("01778", "Market Deeping/Bourne");
			this.areaCodes.put("01779", "Peterhead");
			this.areaCodes.put("01780", "Stamford");
			this.areaCodes.put("01782", "Stoke on Trent");
			this.areaCodes.put("01784", "Staines");
			this.areaCodes.put("01785", "Stafford");
			this.areaCodes.put("01786", "Stirling");
			this.areaCodes.put("01787", "Sudbury");
			this.areaCodes.put("01788", "Rugby");
			this.areaCodes.put("01789", "Stratford upon Avon");
			this.areaCodes.put("01790", "Spilsby");
			this.areaCodes.put("01792", "Swansea");
			this.areaCodes.put("01793", "Swindon");
			this.areaCodes.put("01794", "Romsey");
			this.areaCodes.put("01795", "Sittingbourne, Sheppey");
			this.areaCodes.put("01796", "Pitlochry");
			this.areaCodes.put("01797", "Rye");
			this.areaCodes.put("01798", "Pulborough, Sussex");
			this.areaCodes.put("01799", "Saffron Walden");
			this.areaCodes.put("01803", "Torquay");
			this.areaCodes.put("01805", "Torrington");
			this.areaCodes.put("01806", "Voe, Shetland");
			this.areaCodes.put("01807", "Ballindalloch, Tomintoul");
			this.areaCodes.put("01808", "Tomatin");
			this.areaCodes.put("01809", "Tomdoun");
			this.areaCodes.put("01821", "Kinrossie, Tayside");
			this.areaCodes.put("01822", "Tavistock");
			this.areaCodes.put("01823", "Taunton");
			this.areaCodes.put("01824", "Ruthin, Vale of Clwyd");
			this.areaCodes.put("01825", "Uckfield");
			this.areaCodes.put("01827", "Tamworth");
			this.areaCodes.put("01828", "Coupar Angus, Tayside");
			this.areaCodes.put("01829", "Tarporley");
			this.areaCodes.put("01830", "Kirkwhelpington");
			this.areaCodes.put("01832", "Clopton, Oundle");
			this.areaCodes.put("01833", "Teesdale");
			this.areaCodes.put("01834", "Narberth, Tenby");
			this.areaCodes.put("01835", "St Boswells");
			this.areaCodes.put("01837", "Okehampton");
			this.areaCodes.put("01838", "Dalmally, Tyndrum");
			this.areaCodes.put("01840", "Camelford, Tintagel");
			this.areaCodes.put("01841", "Newquay");
			this.areaCodes.put("01842", "Thetford");
			this.areaCodes.put("01843", "Thanet");
			this.areaCodes.put("01844", "Thame");
			this.areaCodes.put("01845", "Thirsk");
			this.areaCodes.put("01847", "Thurso and Tongue");
			this.areaCodes.put("01848", "Thornhill");
			this.areaCodes.put("01851", "Great Bernera and Stornoway");
			this.areaCodes.put("01852", "Kilmelford");
			this.areaCodes.put("01854", "Ullapool");
			this.areaCodes.put("01855", "Ballachulish");
			this.areaCodes.put("01856", "Orkney");
			this.areaCodes.put("01857", "Sanday");
			this.areaCodes.put("01858", "Market Harborough");
			this.areaCodes.put("01859", "Harris");
			this.areaCodes.put("01862", "Tain");
			this.areaCodes.put("01863", "Ardgay, Tain");
			this.areaCodes.put("01864", "Tinto, Abington");
			this.areaCodes.put("01866", "Kilchrenan");
			this.areaCodes.put("01869", "Bicester, Oxfordshire");
			this.areaCodes.put("01870", "Isle of Benbecula");
			this.areaCodes.put("01871", "Castlebay");
			this.areaCodes.put("01872", "Truro");
			this.areaCodes.put("01873", "Abergavenny, Usk");
			this.areaCodes.put("01874", "Brecon, Usk");
			this.areaCodes.put("01875", "Tranent");
			this.areaCodes.put("01876", "Lochmaddy");
			this.areaCodes.put("01877", "Callander, Trossachs");
			this.areaCodes.put("01878", "Lochboisdale");
			this.areaCodes.put("01879", "Scarinish, Tiree");
			this.areaCodes.put("01880", "Tarbert");
			this.areaCodes.put("01882", "Kinloch Rannoch, Tummel Bridge");
			this.areaCodes.put("01883", "Caterham");
			this.areaCodes.put("01884", "Tiverton");
			this.areaCodes.put("01885", "Pencombe");
			this.areaCodes.put("01886", "Bromyard, Teme Valley");
			this.areaCodes.put("01887", "Aberfeldy, Tay Valley");
			this.areaCodes.put("01888", "Turriff");
			this.areaCodes.put("01889", "Uttoxeter");
			this.areaCodes.put("01890", "Ayton, Berwickshire and Coldstream, Tweed");
			this.areaCodes.put("01892", "Tunbridge Wells");
			this.areaCodes.put("01895", "Uxbridge");
			this.areaCodes.put("01896", "Galashiels, Tweed");
			this.areaCodes.put("01899", "Biggar, Tweed");
			this.areaCodes.put("01900", "Workington");
			this.areaCodes.put("01902", "Wolverhampton");
			this.areaCodes.put("01903", "Worthing");
			this.areaCodes.put("01904", "York");
			this.areaCodes.put("01905", "Worcester");
			this.areaCodes.put("01908", "Milton Keynes, Wolverton");
			this.areaCodes.put("01909", "Worksop");
			this.areaCodes.put("01920", "Ware");
			this.areaCodes.put("01922", "Walsall");
			this.areaCodes.put("01923", "Watford");
			this.areaCodes.put("01924", "Wakefield");
			this.areaCodes.put("01925", "Warrington");
			this.areaCodes.put("01926", "Warwick");
			this.areaCodes.put("01928", "Runcorn, Warrington");
			this.areaCodes.put("01929", "Wareham");
			this.areaCodes.put("01931", "Shap, Westmorland");
			this.areaCodes.put("01932", "Weybridge");
			this.areaCodes.put("01933", "Wellingborough");
			this.areaCodes.put("01934", "Weston-super-Mare");
			this.areaCodes.put("01935", "Yeovil");
			this.areaCodes.put("01937", "Wetherby");
			this.areaCodes.put("01938", "Welshpool");
			this.areaCodes.put("01939", "Wem");
			this.areaCodes.put("01942", "Wigan");
			this.areaCodes.put("01943", "Guiseley, Wharfedale");
			this.areaCodes.put("01944", "West Heslerton");
			this.areaCodes.put("01945", "Wisbech");
			this.areaCodes.put("01946", "Whitehaven");
			this.areaCodes.put("019467", "Gosforth");
			this.areaCodes.put("01947", "Whitby");
			this.areaCodes.put("01948", "Whitchurch");
			this.areaCodes.put("01949", "Whatton");
			this.areaCodes.put("01950", "Sandwick, Yell");
			this.areaCodes.put("01951", "Colonsay");
			this.areaCodes.put("01952", "Telford, Wellington");
			this.areaCodes.put("01953", "Wymondham");
			this.areaCodes.put("01954", "Madingley, Willingham");
			this.areaCodes.put("01955", "Wick");
			this.areaCodes.put("01957", "Mid Yell, Yell");
			this.areaCodes.put("01959", "Westerham, West Kent");
			this.areaCodes.put("01962", "Winchester");
			this.areaCodes.put("01963", "Wincanton");
			this.areaCodes.put("01964", "Hornsea and Patrington, Withernsea");
			this.areaCodes.put("01967", "Strontian");
			this.areaCodes.put("01968", "Penicuik, West Linton");
			this.areaCodes.put("01969", "Leyburn, Wensleydale");
			this.areaCodes.put("01970", "Aberystwyth, Ystwyth");
			this.areaCodes.put("01971", "Scourie, Wrath");
			this.areaCodes.put("01972", "Glenborrodale");
			this.areaCodes.put("01974", "Llanon, Ystwyth");
			this.areaCodes.put("01975", "Alford, Aberdeenshire and Strathdon, Water");
			this.areaCodes.put("01977", "Pontefract, West Riding");
			this.areaCodes.put("01978", "Wrexham");
			this.areaCodes.put("01980", "Amesbury, Wiltshire");
			this.areaCodes.put("01981", "Wormbridge");
			this.areaCodes.put("01982", "Builth Wells");
			this.areaCodes.put("01983", "Isle of Wight");
			this.areaCodes.put("01984", "Watchet");
			this.areaCodes.put("01985", "Warminster");
			this.areaCodes.put("01986", "Bungay, Waveney");
			this.areaCodes.put("01987", "Ebbsfleet [7]");
			this.areaCodes.put("01988", "Wigtown");
			this.areaCodes.put("01989", "Ross on Wye, Wye");
			this.areaCodes.put("01992", "Lea Valley, Waltham X");
			this.areaCodes.put("01993", "Witney");
			this.areaCodes.put("01994", "St Clears, West Wales");
			this.areaCodes.put("01995", "Garstang, Wyre");
			this.areaCodes.put("01997", "Strathpeffer, Wyvis");
			this.areaCodes.put("055", "Voice over IP");
			this.areaCodes.put("056", "Voice over IP");
			this.areaCodes.put("070", "Personal number (can be redirected)");
			this.areaCodes.put("075", "Mobile");
			this.areaCodes.put("077", "Mobile");
			this.areaCodes.put("078", "Mobile");
			this.areaCodes.put("079", "Mobile");
			this.areaCodes.put("080", "Freephone");
			this.areaCodes.put("084", "Local rate");
			this.areaCodes.put("087", "Local rate");
			this.areaCodes.put("09", "Premium rate");
		}
		phoneNumber = phoneNumber.replaceAll(" ", "");
		String possibleCode = "";
		// stop substring creating an IndexOutOfBoundsException below
		if (phoneNumber.length() < 7) {
			return "";
		}
		String area = "";
		for (int numDigitsInCode = 6; numDigitsInCode > 2; numDigitsInCode--) {
			possibleCode = phoneNumber.substring(0, numDigitsInCode);
			area = this.areaCodes.get(possibleCode);
			if (area != null) {
				return area;
			}
		}
		return "";
	}

	public SortedMap<BaseField, BaseValue> getAddress(Map<BaseField, BaseValue> tableDataRow) {
		SortedMap<BaseField, BaseValue> address = new TreeMap<BaseField, BaseValue>();
		SortedMap<BaseField, BaseValue> sortedTableDataRows = new TreeMap<BaseField, BaseValue>(
				tableDataRow);
		for (Map.Entry<BaseField, BaseValue> tableDataRowEntry : sortedTableDataRows.entrySet()) {
			BaseField field = tableDataRowEntry.getKey();
			BaseValue value = tableDataRowEntry.getValue();
			if (value instanceof TextValue) {
				// Postcode is a marker for the end of an address
				if (((TextValue) value).isPostcode()) {
					SortedMap<BaseField, BaseValue> toPostcode = new TreeMap<BaseField, BaseValue>(
							sortedTableDataRows.headMap(field));
					toPostcode.put(field, value);
					// Found the last part of the address (the postcode)
					// Now find the first - a less accurate task.
					// Start by removing all fields before and including the
					// last non-text field
					// as we know all parts of the address will be text fields
					address = toPostcode;
					BaseField addrField = null;
					BaseField nonTextField = null;
					for (Map.Entry<BaseField, BaseValue> addressRow : toPostcode.entrySet()) {
						addrField = addressRow.getKey();
						if (!(addrField instanceof TextField)) {
							nonTextField = addrField;
						}
					}
					if (nonTextField != null) {
						address = address.tailMap(nonTextField);
						address.remove(nonTextField);
					}
					// An address isn't going to be more than six fields long
					int numExtraneousFields = address.size() - 6;
					for (int i = 0; i < numExtraneousFields; i++) {
						address.remove(address.firstKey());
					}
					// The slightly dodgy part - detect first line of address
					// based on field name
					for (BaseField firstField : address.keySet()) {
						String fieldName = firstField.getFieldName().toLowerCase();
						if (fieldName.startsWith("addr") || fieldName.startsWith("add.")
								|| fieldName.startsWith("house") || fieldName.startsWith("flat")
								|| fieldName.startsWith("street")) {
							address = new TreeMap<BaseField, BaseValue>(address.tailMap(firstField));
							return address;
						}
					}
					// No first line can be detected, treat just the postcode on
					// its own as the full address
					address = new TreeMap<BaseField, BaseValue>(address.tailMap(address.lastKey()));
					return address;
				}
			}
		}
		return address;
	}

	public Set<FieldTypeDescriptorInfo> getFieldTypeDescriptors() {
		Set<FieldTypeDescriptorInfo> fieldTypeDescriptors = new LinkedHashSet<FieldTypeDescriptorInfo>();
		for (FieldCategory possibleFieldType : EnumSet
				.allOf(FieldTypeDescriptor.FieldCategory.class)) {
			if (possibleFieldType.isEnabled()) {
				FieldTypeDescriptorInfo fieldTypeDescriptor = new FieldTypeDescriptor(
						possibleFieldType);
				fieldTypeDescriptors.add(fieldTypeDescriptor);
			}
		}
		return fieldTypeDescriptors;
	}

	public Set<FilterTypeDescriptorInfo> getFilterTypeDescriptors() {
		Set<FilterTypeDescriptorInfo> filterTypeDescriptors = new LinkedHashSet<FilterTypeDescriptorInfo>();
		for (FilterType possibleFilterType : EnumSet.allOf(FilterType.class)) {
			FilterTypeDescriptorInfo filterTypeDescriptor = new FilterTypeDescriptor(
					possibleFilterType);
			filterTypeDescriptors.add(filterTypeDescriptor);
		}
		return filterTypeDescriptors;
	}

	public void log(Object itemToLog) {
		logger.info("Template message at " + System.currentTimeMillis() + ": " + itemToLog);
	}

	public void startTimer(String timerName) {
		if (AppProperties.enableTemplateTimers) {
			this.timers.put(timerName, BigInteger.valueOf(System.currentTimeMillis()));
		}
	}

	public void stopTimer(String timerName) {
		if (AppProperties.enableTemplateTimers) {
			BigInteger startTime = this.timers.get(timerName);
			if (startTime == null) {
				logger.warn("Timer '" + timerName + "' has not been started");
				return;
			}
			this.timers.remove(timerName);
			long elapsedTime = System.currentTimeMillis() - startTime.longValue();
			this.log(timerName + " elapsed time = " + elapsedTime + "ms");
		}
	}

	public String escape(String string) {
		if (string == null) {
			return "";
		}
		// What a rubbish Java regex!
		String escapedString = string.replaceAll("'", "\\\\'");
		escapedString = escapedString.replaceAll("\\r", "");
		escapedString = escapedString.replaceAll("\\n", "");
		return escapedString;
	}

	public String escapeForCSV(String string) {
		if (string == null) {
			return "";
		}
		String escapedString = string.replaceAll("\"", "\"\"");
		if (escapedString.contains(",") || escapedString.contains("\n")) {
			escapedString = "\"" + escapedString + "\"";
		}
		return escapedString;
	}

	// TODO: rename method to urlEncode
	public String escapeForURL(String string) {
		String encoded = string;
		try {
			// URLEncoder.encode replaces spaces with plus signs which is not what we want
			encoded = string.replaceAll("\\s", "gtpb_special_variable_space");
			// Only encode content after the path
			String path = encoded.replaceAll("\\/.*$", "");
			String filename = encoded.replaceAll("^.*\\/", "");
			encoded = path + "/" + java.net.URLEncoder.encode(filename, "UTF-8");
			encoded = encoded.replace("gtpb_special_variable_space", "%20");
		} catch (UnsupportedEncodingException e) {
			logger.error("Error URL encoding string '" + string + "': " + e);
		}
		return encoded;
	}

	public String joinWith(Collection<Object> collection, String joiner) {
		String result = "";
		for (Object obj : collection) {
			result = result + obj + joiner;
		}
		if (result.length() > joiner.length()) {
			result = result.substring(0, result.length() - joiner.length());
		}
		return result;
	}

	public MathTool getMathTool() {
		return this.mathTool;
	}

	public TextValue getTextValueTool(String text) {
		return new TextValueDefn(text);
	}

	public FileValue getFileValueTool(String filename) {
		return new FileValueDefn(filename);
	}

	public Double roundTo(int decimalPlaces, double number) {
		BigDecimal bigDecimal = new BigDecimal(number);
		String stringRepresentation = bigDecimal.toPlainString();
		stringRepresentation = stringRepresentation.replaceAll("^\\-", "");
		stringRepresentation = stringRepresentation.replaceAll("\\.\\d+$", "");
		int precision = stringRepresentation.length() + decimalPlaces;
		MathContext mc = new MathContext(precision, RoundingMode.HALF_UP);
		bigDecimal = bigDecimal.round(mc);
		return bigDecimal.doubleValue();
	}

	public Browsers getBrowser() {
		String userAgent = request.getHeader("User-Agent").toLowerCase();
		// The user agent may match multiple browsers, e.g. the iPhone will
		// trigger IPHONE and SAFARI
		EnumSet<Browsers> browsersMatched = EnumSet.noneOf(Browsers.class);
		for (Browsers browser : EnumSet.allOf(Browsers.class)) {
			if (userAgent.contains(browser.getUserAgentString())) {
				browsersMatched.add(browser);
			}
		}
		// Treat the iphone and ipod as one
		if (browsersMatched.contains(Browsers.IPHONE) || browsersMatched.contains(Browsers.IPOD)) {
			return Browsers.APPLE_MOBILE;
		} else {
			for (Browsers browser : browsersMatched) {
				return browser;
			}
		}
		return Browsers.UNKNOWN;
	}

	public boolean browserVersionIsAtLeast(String testVersionString) {
		float testVersion = Float.valueOf(testVersionString);
		float detectedVersion = this.getBrowserVersion();
		if (detectedVersion >= testVersion) {
			return true;
		} else {
			return false;
		}
	}

	public float getBrowserVersion() {
		String userAgent = this.request.getHeader("User-Agent").toLowerCase();
		Browsers browser = this.getBrowser();
		String versionString = "";
		float detectedVersion = 0.0f;
		// Firefox variants
		if (browser.equals(Browsers.FIREFOX) || browser.equals(Browsers.MINEFIELD)
				|| browser.equals(Browsers.CAMINO)) {
			// example user agents
			// Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.0.1)
			// Gecko/20060111 Firefox/1.5.0.1
			// Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.7.6) Gecko/20050306
			// Firefox/1.0.1 (Debian package 1.0.1-2)
			versionString = userAgent.replaceAll("^.*" + browser.getUserAgentString() + "\\/", "");
			versionString = versionString.replaceAll("\\s.*$", "");
			// version string will now be something like 1.5.0.2
			// Just in case it's an alpha or beta like 3.0b1, remove the
			// designation
			versionString = versionString.replaceAll("[a-z]", "");
			// get the number before the first decimal point
			String majorVersionString = (new StringBuilder(versionString)).reverse().toString();
			// majorVersion is now 2.0.5.1
			majorVersionString = majorVersionString.replaceAll("^.*\\.", "");
			majorVersionString = (new StringBuilder(majorVersionString)).reverse().toString();
			// majorVersion is now 1
			String minorVersionString = versionString.replaceFirst(
					"^" + majorVersionString + "\\.", "");
			minorVersionString = minorVersionString.replaceAll("\\.", "");
			// minorVersion is now 502
			detectedVersion = Float.valueOf(majorVersionString + "." + minorVersionString);
		} else if (browser.equals(Browsers.MSIE)) {
			// example user agent
			// Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR
			// 1.0.3705; .NET CLR 1.1.4322)
			versionString = userAgent.replaceAll("^.*" + browser.getUserAgentString() + "\\s",
					browser.getUserAgentString());
			versionString = versionString.replaceAll("\\;.*$", "");
			versionString = versionString.replaceAll(browser.getUserAgentString(), "");
			detectedVersion = Float.valueOf(versionString);
		} else if (browser.equals(Browsers.SAFARI)) {
			// e.g. Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en)
			// AppleWebKit/417.9 (KHTML, like Gecko) Safari/417.8
			// iPhone example:
			// Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us)
			// AppleWebKit/523.10.3 (KHTML, like Gecko)
			versionString = userAgent.replaceAll("^.*" + browser.getUserAgentString() + "\\/", "");
			versionString = versionString.replaceAll("^\\s.*", "");
			String majorVersionString = (new StringBuilder(versionString)).reverse().toString();
			majorVersionString = majorVersionString.replaceAll("^.*\\.", "");
			majorVersionString = (new StringBuilder(majorVersionString)).reverse().toString();
			String minorVersionString = versionString.replaceFirst(
					"^" + majorVersionString + "\\.", "");
			minorVersionString = minorVersionString.replaceAll("\\.", "");
			detectedVersion = Float.valueOf(majorVersionString + "." + minorVersionString);
		} else if (browser.equals(Browsers.OPERA)) {
			// Opera/8.02 (Macintosh; PPC Mac OS X; U; en)
			versionString = userAgent.replaceAll("^.*" + browser.getUserAgentString() + "\\/", "");
			versionString = versionString.replaceAll("\\s.*$", "");
			detectedVersion = Float.valueOf(versionString);
		} else if (browser.equals(Browsers.SYMBIAN_MOBILE)) {
			// TODO: Symbian Safari version detect
			return 1.0f;
		} else {
			logger.warn("Unable to detect browser version from " + userAgent);
		}
		return detectedVersion;
	}

	public boolean isRunningLocally() {
		String remoteIP = this.request.getRemoteAddr();
		if (remoteIP.equals("127.0.0.1")) {
			return true;
		} else {
			return false;
		}
	}

	public Map<BaseField, String> getNewBaseFieldStringMap() {
		return this.getNewFilterMap();
	}

	public SortedMap<String, Object> getNewSortedStringObjectMap() {
		return new TreeMap<String, Object>();
	}

	public SortedMap<ModuleInfo, Object> getNewSortedModuleObjectMap() {
		return new TreeMap<ModuleInfo, Object>();
	}

	public Map<BaseField, String> getNewFilterMap() {
		return new HashMap<BaseField, String>();
	}

	public Map<String, Object> getNewStringObjectMap() {
		return new HashMap<String, Object>();
	}

	public Set<String> getNewStringSet() {
		return new HashSet<String>();
	}

	public void reverseList(List list) {
		Collections.reverse(list);
	}

	public void sortList(List list) {
		Collections.sort(list);
	}

	public Map getRequestParameters() {
		return this.request.getParameterMap();
	}

	public String getContentType() {
		return this.response.getContentType();
	}

	public String getRandomString() {
		return new RandomString().toString();
	}

	public String getAppUrl() {
		String appUrl = "";
		if (this.request.isSecure()) {
			appUrl = "https://";
		} else {
			appUrl = "http://";
		}
		String serverName = this.request.getServerName();
		appUrl += serverName;
		int port = this.request.getServerPort();
		if ((port != 80) && (!this.request.isSecure())) {
			appUrl += ":" + port;
		}
		appUrl += this.request.getContextPath() + this.request.getServletPath();
		return appUrl;
	}

	public String lpad(String stringToPad, int lengthToPadTo, String padCharacter) {
		String paddedString = stringToPad;
		int numExtraCharsNecessary = lengthToPadTo - stringToPad.length();
		for (int i = 0; i < numExtraCharsNecessary; i++) {
			paddedString = padCharacter + paddedString;
		}
		return paddedString;
	}

	public String cleanString(String stringToClean) {
		return stringToClean.toLowerCase().replaceAll("\\W", "");
	}

	public String rinseString(String stringToRinse) {
		return Helpers.rinseString(stringToRinse);
	}

	public String lineBreaksToParas(String stringToConvert) {
		return stringToConvert.replaceAll("\n", "<p>");
	}

	public boolean templateExists(String templateFilename) {
		String absoluteFilename = this.request.getSession().getServletContext().getRealPath(
				"/WEB-INF/templates/" + templateFilename);
		File templateFile = new File(absoluteFilename);
		return templateFile.exists();
	}

	public List<File> listFiles(String folderName) {
		String absoluteFolderName = this.request.getSession().getServletContext().getRealPath(
				"/" + folderName);
		File folder = new File(absoluteFolderName);
		File[] filesArray = folder.listFiles();
		List<File> files = new LinkedList<File>();
		if (filesArray != null) {
			files = Arrays.asList(filesArray);
			Collections.sort(files);
		}
		return files;
	}

	public String toString() {
		return "ViewTools contains utility methods useful to Velocity template designers";
	}

	public void throwException() throws CantDoThatException {
		throw new CantDoThatException("Test error message");
	}

	private HttpServletRequest request = null;

	private HttpServletResponse response = null;

	private String webAppRoot = null;

	private MathTool mathTool = new MathTool();

	private Map<String, BigInteger> timers = new HashMap<String, BigInteger>();

	/**
	 * A map of telephone area code to city / location
	 */
	private Map<String, String> areaCodes = new HashMap<String, String>(602);

	private static final SimpleLogger logger = new SimpleLogger(ViewTools.class);

}
