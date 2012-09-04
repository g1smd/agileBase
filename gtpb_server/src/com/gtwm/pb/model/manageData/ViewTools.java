/*
 *  Copyright 2012 GT webMarque Ltd
 *
 *  This file is part of agileBase.
 *
 *  agileBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  agileBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with agileBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.manageData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.MathContext;
import java.nio.charset.Charset;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ViewToolsInfo;
import com.gtwm.pb.model.interfaces.FilterTypeDescriptorInfo;
import com.gtwm.pb.model.manageSchema.FilterTypeDescriptor;
import com.gtwm.pb.util.Enumerations.FilterType;
import com.gtwm.pb.util.Enumerations.Browsers;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.ObjectNotFoundException;
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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.velocity.tools.generic.MathTool;
import org.grlea.log.SimpleLogger;
import com.ibm.icu.text.RuleBasedNumberFormat;

public final class ViewTools implements ViewToolsInfo {

	private ViewTools() {
		this.webAppRoot = null;
		this.request = null;
		this.response = null;
	}

	public ViewTools(HttpServletRequest request, HttpServletResponse response, String webAppRoot) {
		this.request = request;
		this.response = response;
		this.webAppRoot = webAppRoot;
	}

	public String getWebAppRoot() {
		return this.webAppRoot;
	}

	public boolean isNull(Object o) {
		return (o == null);
	}

	public boolean isInteger(String string) {
		return (string.matches("\\d+"));
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
		if (this.areaCodes.isEmpty()) {
			// initialise array, values from wikipedia and @g1smd
			// this is a list of initial digits not area codes
			this.areaCodes.put("200", "London");
			this.areaCodes.put("201", "London");
			this.areaCodes.put("203", "London");
			this.areaCodes.put("207", "London");
			this.areaCodes.put("208", "London");
			this.areaCodes.put("230", "Southampton and Portsmouth");
			this.areaCodes.put("231", "Southampton and Portsmouth");
			this.areaCodes.put("238", "Southampton");
			this.areaCodes.put("239", "Portsmouth");
			this.areaCodes.put("240", "Coventry");
			this.areaCodes.put("241", "Coventry");
			this.areaCodes.put("247", "Coventry");
			this.areaCodes.put("28", "Northern Ireland");
			this.areaCodes.put("290", "Cardiff");
			this.areaCodes.put("291", "Cardiff");
			this.areaCodes.put("292", "Cardiff");
			this.areaCodes.put("113", "Leeds");
			this.areaCodes.put("114", "Sheffield");
			this.areaCodes.put("115", "Nottingham");
			this.areaCodes.put("116", "Leicester");
			this.areaCodes.put("117", "Bristol");
			this.areaCodes.put("118", "Reading");
			this.areaCodes.put("121", "Birmingham");
			this.areaCodes.put("131", "Edinburgh");
			this.areaCodes.put("141", "Glasgow");
			this.areaCodes.put("151", "Liverpool");
			this.areaCodes.put("161", "Manchester");
			this.areaCodes.put("191", "Tyneside, Sunderland and Durham");
			this.areaCodes.put("1912", "Tyneside");
			this.areaCodes.put("1914", "Tyneside");
			this.areaCodes.put("1916", "Tyneside");
			this.areaCodes.put("1918", "Tyneside");
			this.areaCodes.put("1913", "Durham");
			this.areaCodes.put("1919", "Durham");
			this.areaCodes.put("1915", "Sunderland");
			this.areaCodes.put("1917", "Sunderland");
			this.areaCodes.put("1200", "Clitheroe");
			this.areaCodes.put("1202", "Bournemouth");
			this.areaCodes.put("1204", "Bolton");
			this.areaCodes.put("1205", "Boston");
			this.areaCodes.put("1206", "Colchester");
			this.areaCodes.put("1207", "Consett");
			this.areaCodes.put("1208", "Bodmin");
			this.areaCodes.put("1209", "Redruth, Cornwall");
			this.areaCodes.put("1223", "Cambridge");
			this.areaCodes.put("1224", "Aberdeen");
			this.areaCodes.put("1225", "Bath");
			this.areaCodes.put("1226", "Barnsley");
			this.areaCodes.put("1227", "Canterbury");
			this.areaCodes.put("1228", "Carlisle");
			this.areaCodes.put("1229", "Barrow-in-Furness and Millom");
			this.areaCodes.put("1233", "Ashford, Kent");
			this.areaCodes.put("1234", "Bedford");
			this.areaCodes.put("1235", "Abingdon");
			this.areaCodes.put("1236", "Coatbridge");
			this.areaCodes.put("1237", "Bideford");
			this.areaCodes.put("1239", "Cardigan");
			this.areaCodes.put("1241", "Arbroath");
			this.areaCodes.put("1242", "Cheltenham");
			this.areaCodes.put("1243", "Chichester, West Sussex");
			this.areaCodes.put("1244", "Chester");
			this.areaCodes.put("1245", "Chelmsford");
			this.areaCodes.put("1246", "Chesterfield");
			this.areaCodes.put("1248", "Bangor, Gwynedd");
			this.areaCodes.put("1249", "Chippenham");
			this.areaCodes.put("1250", "Blairgowrie");
			this.areaCodes.put("1252", "Aldershot");
			this.areaCodes.put("1253", "Blackpool");
			this.areaCodes.put("1254", "Blackburn");
			this.areaCodes.put("1255", "Clacton-on-Sea");
			this.areaCodes.put("1256", "Basingstoke");
			this.areaCodes.put("1257", "Coppull, Chorley");
			this.areaCodes.put("1258", "Blandford");
			this.areaCodes.put("1259", "Alloa");
			this.areaCodes.put("1260", "Congleton");
			this.areaCodes.put("1261", "Banff");
			this.areaCodes.put("1262", "Bridlington");
			this.areaCodes.put("1263", "Cromer");
			this.areaCodes.put("1264", "Andover");
			this.areaCodes.put("1267", "Carmarthen");
			this.areaCodes.put("1268", "Basildon");
			this.areaCodes.put("1269", "Ammanford");
			this.areaCodes.put("1270", "Crewe");
			this.areaCodes.put("1271", "Barnstaple");
			this.areaCodes.put("1273", "Brighton");
			this.areaCodes.put("1274", "Bradford");
			this.areaCodes.put("1275", "Clevedon, Bristol");
			this.areaCodes.put("1276", "Camberley");
			this.areaCodes.put("1277", "Brentwood");
			this.areaCodes.put("1278", "Bridgwater");
			this.areaCodes.put("1279", "Bishop's Stortford");
			this.areaCodes.put("1280", "Buckingham");
			this.areaCodes.put("1282", "Burnley");
			this.areaCodes.put("1283", "Burton-on-Trent");
			this.areaCodes.put("1284", "Bury St Edmunds");
			this.areaCodes.put("1285", "Cirencester");
			this.areaCodes.put("1286", "Caernarfon");
			this.areaCodes.put("1287", "Guisborough");
			this.areaCodes.put("1288", "Bude");
			this.areaCodes.put("1289", "Berwick-upon-Tweed");
			this.areaCodes.put("1290", "Cumnock, Ayrshire");
			this.areaCodes.put("1291", "Chepstow");
			this.areaCodes.put("1292", "Ayr");
			this.areaCodes.put("1293", "Crawley");
			this.areaCodes.put("1294", "Ardrossan, Ayrshire");
			this.areaCodes.put("1295", "Banbury");
			this.areaCodes.put("1296", "Aylesbury");
			this.areaCodes.put("1297", "Axminster");
			this.areaCodes.put("1298", "Buxton");
			this.areaCodes.put("1299", "Bewdley");
			this.areaCodes.put("1300", "Cerne Abbas, Dorset");
			this.areaCodes.put("1301", "Arrochar");
			this.areaCodes.put("1302", "Doncaster");
			this.areaCodes.put("1303", "Folkestone");
			this.areaCodes.put("1304", "Dover");
			this.areaCodes.put("1305", "Dorchester");
			this.areaCodes.put("1306", "Dorking");
			this.areaCodes.put("1307", "Forfar");
			this.areaCodes.put("1308", "Bridport, Dorset");
			this.areaCodes.put("1309", "Forres");
			this.areaCodes.put("1320", "Fort Augustus");
			this.areaCodes.put("1322", "Dartford");
			this.areaCodes.put("1323", "Eastbourne");
			this.areaCodes.put("1324", "Falkirk");
			this.areaCodes.put("1325", "Darlington");
			this.areaCodes.put("1326", "Falmouth");
			this.areaCodes.put("1327", "Daventry");
			this.areaCodes.put("1328", "Fakenham");
			this.areaCodes.put("1329", "Fareham");
			this.areaCodes.put("1330", "Banchory, Deeside");
			this.areaCodes.put("1332", "Derby");
			this.areaCodes.put("1333", "Peat Inn and Leven, Fife");
			this.areaCodes.put("1334", "St Andrews, Fife");
			this.areaCodes.put("1335", "Ashbourne");
			this.areaCodes.put("1337", "Ladybank, Fife");
			this.areaCodes.put("1339", "Aboyne and Ballater");
			this.areaCodes.put("1340", "Craigellachie, Elgin");
			this.areaCodes.put("1341", "Barmouth, Dolgellau");
			this.areaCodes.put("1342", "East Grinstead");
			this.areaCodes.put("1343", "Elgin");
			this.areaCodes.put("1344", "Bracknell, Easthampstead");
			this.areaCodes.put("1346", "Fraserburgh");
			this.areaCodes.put("1347", "Easingwold");
			this.areaCodes.put("1348", "Fishguard");
			this.areaCodes.put("1349", "Dingwall");
			this.areaCodes.put("1350", "Dunkeld");
			this.areaCodes.put("1352", "Mold, Flint");
			this.areaCodes.put("1353", "Ely");
			this.areaCodes.put("1354", "Chatteris and March, Cambridgeshire, Fenland");
			this.areaCodes.put("1355", "East Kilbride");
			this.areaCodes.put("1356", "Brechin, Edzell");
			this.areaCodes.put("1357", "Strathaven, East Kilbride");
			this.areaCodes.put("1358", "Ellon");
			this.areaCodes.put("1359", "Pakenham, Elmswell");
			this.areaCodes.put("1360", "Killearn, Drymen");
			this.areaCodes.put("1361", "Duns");
			this.areaCodes.put("1362", "Dereham");
			this.areaCodes.put("1363", "Crediton");
			this.areaCodes.put("1364", "Ashburton, Devon");
			this.areaCodes.put("1366", "Downham Market");
			this.areaCodes.put("1367", "Faringdon");
			this.areaCodes.put("1368", "Dunbar");
			this.areaCodes.put("1369", "Dunoon");
			this.areaCodes.put("1371", "Great Dunmow, Essex");
			this.areaCodes.put("1372", "Esher, Epsom");
			this.areaCodes.put("1373", "Frome");
			this.areaCodes.put("1375", "Grays Thurrock, Essex");
			this.areaCodes.put("1376", "Braintree, Essex");
			this.areaCodes.put("1377", "Driffield");
			this.areaCodes.put("1379", "Diss");
			this.areaCodes.put("1380", "Devizes");
			this.areaCodes.put("1381", "Fortrose");
			this.areaCodes.put("1382", "Dundee");
			this.areaCodes.put("1383", "Dunfermline");
			this.areaCodes.put("1384", "Dudley");
			this.areaCodes.put("1386", "Evesham");
			this.areaCodes.put("1387", "Dumfries");
			this.areaCodes.put("13873", "Langholm");
			this.areaCodes.put("1388", "Bishop Auckland, Durham and Stanhope (Eastgate)");
			this.areaCodes.put("1389", "Dumbarton");
			this.areaCodes.put("1392", "Exeter");
			this.areaCodes.put("1394", "Felixstowe");
			this.areaCodes.put("1395", "Budleigh Salterton, Exmouth");
			this.areaCodes.put("1397", "Fort William");
			this.areaCodes.put("1398", "Dulverton, Exmoor");
			this.areaCodes.put("1400", "Honington");
			this.areaCodes.put("1403", "Horsham");
			this.areaCodes.put("1404", "Honiton");
			this.areaCodes.put("1405", "Goole");
			this.areaCodes.put("1406", "Holbeach");
			this.areaCodes.put("1407", "Holyhead");
			this.areaCodes.put("1408", "Golspie");
			this.areaCodes.put("1409", "Holsworthy");
			this.areaCodes.put("1420", "Alton");
			this.areaCodes.put("1422", "Halifax");
			this.areaCodes.put("1423", "Boroughbridge and Harrogate");
			this.areaCodes.put("1424", "Hastings");
			this.areaCodes.put("1425", "Ringwood, Highcliffe; New Milton, Ashley");
			this.areaCodes.put("1427", "Gainsborough");
			this.areaCodes.put("1428", "Haslemere");
			this.areaCodes.put("1429", "Hartlepool");
			this.areaCodes.put("1430", "Market Weighton and North Cave, Howden");
			this.areaCodes.put("1431", "Helmsdale");
			this.areaCodes.put("1432", "Hereford");
			this.areaCodes.put("1433", "Hathersage");
			this.areaCodes.put("1434", "Bellingham, Haltwhistle and Hexham");
			this.areaCodes.put("1435", "Heathfield");
			this.areaCodes.put("1436", "Helensburgh");
			this.areaCodes.put("1437", "Clynderwen and Haverfordwest");
			this.areaCodes.put("1438", "Stevenage, Hertfordshire");
			this.areaCodes.put("1439", "Helmsley");
			this.areaCodes.put("1440", "Haverhill");
			this.areaCodes.put("1442", "Hemel Hempstead");
			this.areaCodes.put("1443", "Pontypridd, Glamorgan");
			this.areaCodes.put("1444", "Haywards Heath");
			this.areaCodes.put("1445", "Gairloch");
			this.areaCodes.put("1446", "Barry, Glamorgan");
			this.areaCodes.put("1449", "Stowmarket, Gipping");
			this.areaCodes.put("1450", "Hawick");
			this.areaCodes.put("1451", "Stow-on-the-Wold, Gloucestershire");
			this.areaCodes.put("1452", "Gloucester");
			this.areaCodes.put("1453", "Dursley, Gloucestershire");
			this.areaCodes.put("1454", "Chipping Sodbury, Gloucestershire");
			this.areaCodes.put("1455", "Hinckley");
			this.areaCodes.put("1456", "Glenurquhart");
			this.areaCodes.put("1457", "Glossop");
			this.areaCodes.put("1458", "Glastonbury");
			this.areaCodes.put("1460", "Chard, Ilminster");
			this.areaCodes.put("1461", "Gretna");
			this.areaCodes.put("1462", "Hitchin");
			this.areaCodes.put("1463", "Inverness");
			this.areaCodes.put("1464", "Insch");
			this.areaCodes.put("1465", "Girvan");
			this.areaCodes.put("1466", "Huntly");
			this.areaCodes.put("1467", "Inverurie");
			this.areaCodes.put("1469", "Killingholme, Immingham");
			this.areaCodes.put("1470", "Edinbane, Isle of Skye");
			this.areaCodes.put("1471", "Broadford, Isle of Skye");
			this.areaCodes.put("1472", "Grimsby");
			this.areaCodes.put("1473", "Ipswich");
			this.areaCodes.put("1474", "Gravesend");
			this.areaCodes.put("1475", "Greenock");
			this.areaCodes.put("1476", "Grantham");
			this.areaCodes.put("1477", "Holmes Chapel");
			this.areaCodes.put("1478", "Portree, Isle of Skye");
			this.areaCodes.put("1479", "Grantown-on-Spey");
			this.areaCodes.put("1480", "Huntingdon");
			this.areaCodes.put("1481", "Guernsey");
			this.areaCodes.put("1482", "Kingston-upon-Hull");
			this.areaCodes.put("1483", "Guildford");
			this.areaCodes.put("1484", "Huddersfield");
			this.areaCodes.put("1485", "Hunstanton");
			this.areaCodes.put("1487", "Warboys, Huntingdon");
			this.areaCodes.put("1488", "Hungerford");
			this.areaCodes.put("1489", "Bishops Waltham, Hamble Valley");
			this.areaCodes.put("1490", "Corwen, Gwynedd");
			this.areaCodes.put("1491", "Henley-on-Thames");
			this.areaCodes.put("1492", "Colwyn Bay, Gwynedd");
			this.areaCodes.put("1493", "Great Yarmouth");
			this.areaCodes.put("1494", "High Wycombe");
			this.areaCodes.put("1495", "Pontypool, Gwent");
			this.areaCodes.put("1496", "Port Ellen, Islay");
			this.areaCodes.put("1497", "Hay-on-Wye");
			this.areaCodes.put("1499", "Inveraray");
			this.areaCodes.put("1501", "Harthill, Lothian");
			this.areaCodes.put("1502", "Lowestoft");
			this.areaCodes.put("1503", "Looe");
			this.areaCodes.put("1505", "Johnstone");
			this.areaCodes.put("1506", "Bathgate, Lothian");
			this.areaCodes.put("1507", "Alford (Lincs), Louth and Spilsby (Horncastle)");
			this.areaCodes.put("1508", "Brooke");
			this.areaCodes.put("1509", "Loughborough");
			this.areaCodes.put("1520", "Lochcarron");
			this.areaCodes.put("1522", "Lincoln");
			this.areaCodes.put("1524", "Lancaster");
			this.areaCodes.put("15242", "Hornby");
			this.areaCodes.put("1525", "Leighton Buzzard");
			this.areaCodes.put("1526", "Martin, Lincolnshire");
			this.areaCodes.put("1527", "Redditch");
			this.areaCodes.put("1528", "Laggan, Badenoch");
			this.areaCodes.put("1529", "Sleaford, Lincolnshire");
			this.areaCodes.put("1530", "Coalville, Ashby-de-la-Zouch, Leicestershire");
			this.areaCodes.put("1531", "Ledbury");
			this.areaCodes.put("1534", "Jersey");
			this.areaCodes.put("1535", "Keighley");
			this.areaCodes.put("1536", "Kettering");
			this.areaCodes.put("1538", "Ipstones, Leek");
			this.areaCodes.put("1539", "Kendal");
			this.areaCodes.put("15394", "Hawkshead");
			this.areaCodes.put("15395", "Grange-over-Sands");
			this.areaCodes.put("15396", "Sedbergh");
			this.areaCodes.put("1540", "Kingussie");
			this.areaCodes.put("1542", "Keith");
			this.areaCodes.put("1543", "Cannock, Lichfield");
			this.areaCodes.put("1544", "Kington");
			this.areaCodes.put("1545", "Llanarth, Ceredigion");
			this.areaCodes.put("1546", "Lochgilphead");
			this.areaCodes.put("1547", "Knighton");
			this.areaCodes.put("1548", "Kingsbridge");
			this.areaCodes.put("1549", "Lairg");
			this.areaCodes.put("1550", "Llandovery");
			this.areaCodes.put("1553", "King's Lynn");
			this.areaCodes.put("1554", "Llanelli");
			this.areaCodes.put("1555", "Lanark");
			this.areaCodes.put("1556", "Castle Douglas, Kirkcudbrightshire");
			this.areaCodes.put("1557", "Kirkcudbright");
			this.areaCodes.put("1558", "Llandeilo");
			this.areaCodes.put("1559", "Llandysul");
			this.areaCodes.put("1560", "Moscow");
			this.areaCodes.put("1561", "Laurencekirk");
			this.areaCodes.put("1562", "Kidderminster");
			this.areaCodes.put("1563", "Kilmarnock");
			this.areaCodes.put("1564", "Lapworth, Knowle");
			this.areaCodes.put("1565", "Knutsford");
			this.areaCodes.put("1566", "Launceston");
			this.areaCodes.put("1567", "Killin");
			this.areaCodes.put("1568", "Leominster");
			this.areaCodes.put("1569", "Stonehaven, Laurencekirk");
			this.areaCodes.put("1570", "Lampeter");
			this.areaCodes.put("1571", "Lochinver");
			this.areaCodes.put("1572", "Oakham");
			this.areaCodes.put("1573", "Kelso");
			this.areaCodes.put("1575", "Kirriemuir");
			this.areaCodes.put("1576", "Lockerbie");
			this.areaCodes.put("1577", "Kinross");
			this.areaCodes.put("1578", "Lauder");
			this.areaCodes.put("1579", "Liskeard");
			this.areaCodes.put("1580", "Cranbrook, Kent");
			this.areaCodes.put("1581", "New Luce, Luce");
			this.areaCodes.put("1582", "Luton");
			this.areaCodes.put("1583", "Carradale, Kintyre");
			this.areaCodes.put("1584", "Ludlow");
			this.areaCodes.put("1586", "Campbeltown, Kintyre");
			this.areaCodes.put("1588", "Bishops Castle, Ludlow");
			this.areaCodes.put("1590", "Lymington");
			this.areaCodes.put("1591", "Llanwrtyd Wells");
			this.areaCodes.put("1592", "Kirkcaldy");
			this.areaCodes.put("1593", "Lybster");
			this.areaCodes.put("1594", "Lydney");
			this.areaCodes.put("1595", "Lerwick, Foula and Fair Isle");
			this.areaCodes.put("1597", "Llandrindod Wells");
			this.areaCodes.put("1598", "Lynton");
			this.areaCodes.put("1599", "Kyle");
			this.areaCodes.put("1600", "Monmouth");
			this.areaCodes.put("1603", "Norwich");
			this.areaCodes.put("1604", "Northampton");
			this.areaCodes.put("1606", "Northwich");
			this.areaCodes.put("1608", "Chipping Norton");
			this.areaCodes.put("1609", "Northallerton");
			this.areaCodes.put("1620", "North Berwick");
			this.areaCodes.put("1621", "Maldon");
			this.areaCodes.put("1622", "Maidstone");
			this.areaCodes.put("1623", "Mansfield");
			this.areaCodes.put("1624", "Isle of Man");
			this.areaCodes.put("1625", "Macclesfield");
			this.areaCodes.put("1626", "Newton Abbot");
			this.areaCodes.put("1628", "Maidenhead");
			this.areaCodes.put("1629", "Matlock");
			this.areaCodes.put("1630", "Market Drayton");
			this.areaCodes.put("1631", "Oban");
			this.areaCodes.put("1632",
					"numbers are now used as fictional numbers for drama purposes");
			this.areaCodes.put("1633", "Newport");
			this.areaCodes.put("1634", "Medway");
			this.areaCodes.put("1635", "Newbury");
			this.areaCodes.put("1636", "Newark-on-Trent");
			this.areaCodes.put("1637", "Newquay");
			this.areaCodes.put("1638", "Newmarket");
			this.areaCodes.put("1639", "Neath");
			this.areaCodes.put("1641", "Strathy, Melvich");
			this.areaCodes.put("1642", "Middlesbrough");
			this.areaCodes.put("1643", "Minehead");
			this.areaCodes.put("1644", "New Galloway");
			this.areaCodes.put("1646", "Milford Haven");
			this.areaCodes.put("1647", "Moretonhampstead");
			this.areaCodes.put("1650", "Cemmaes Road, Machynlleth");
			this.areaCodes.put("1651", "Oldmeldrum");
			this.areaCodes.put("1652", "Brigg, North Kelsey");
			this.areaCodes.put("1653", "Malton");
			this.areaCodes.put("1654", "Machynlleth");
			this.areaCodes.put("1655", "Maybole");
			this.areaCodes.put("1656", "Bridgend");
			this.areaCodes.put("1659", "Sanquhar, Nithsdale");
			this.areaCodes.put("1661", "Prudhoe, Northumberland");
			this.areaCodes.put("1663", "New Mills");
			this.areaCodes.put("1664", "Melton Mowbray");
			this.areaCodes.put("1665", "Alnwick, Northumberland");
			this.areaCodes.put("1666", "Malmesbury");
			this.areaCodes.put("1667", "Nairn");
			this.areaCodes.put("1668", "Bamburgh, Northumberland");
			this.areaCodes.put("1669", "Rothbury, Northumberland");
			this.areaCodes.put("1670", "Morpeth");
			this.areaCodes.put("1671", "Newton Stewart");
			this.areaCodes.put("1672", "Marlborough");
			this.areaCodes.put("1673", "Market Rasen");
			this.areaCodes.put("1674", "Montrose");
			this.areaCodes.put("1675", "Coleshill, Warwickshire, Meriden");
			this.areaCodes.put("1676", "Meriden");
			this.areaCodes.put("1677", "Bedale, North Riding");
			this.areaCodes.put("1678", "Bala, Meirionydd");
			this.areaCodes.put("1680", "Craignure, Isle of Mull");
			this.areaCodes.put("1681", "Fionnphort, Isle of Mull");
			this.areaCodes.put("1683", "Moffat");
			this.areaCodes.put("1684", "Malvern");
			this.areaCodes.put("1685", "Merthyr Tydfil");
			this.areaCodes.put("1686", "Llanidloes and Newtown");
			this.areaCodes.put("1687", "Mallaig");
			this.areaCodes.put("1688", "Tobermory, Isle of Mull");
			this.areaCodes.put("1689", "Orpington");
			this.areaCodes.put("1690", "Betws-y-Coed");
			this.areaCodes.put("1691", "Oswestry");
			this.areaCodes.put("1692", "North Walsham");
			this.areaCodes.put("1694", "Church Stretton");
			this.areaCodes.put("1695", "Skelmersdale");
			this.areaCodes.put("1697", "Brampton");
			this.areaCodes.put("16973", "Wigton");
			this.areaCodes.put("16974", "Raughton Head");
			this.areaCodes.put("16977", "Brampton, Hallbankgate");
			this.areaCodes.put("1698", "Motherwell");
			this.areaCodes.put("1700", "Rothesay");
			this.areaCodes.put("1702", "Southend-on-Sea");
			this.areaCodes.put("1704", "Southport");
			this.areaCodes.put("1706", "Rochdale, Rossendale");
			this.areaCodes.put("1707", "Welwyn Garden City, Hatfield and Potters Bar");
			this.areaCodes.put("1708", "Romford");
			this.areaCodes.put("1709", "Rotherham");
			this.areaCodes.put("1720", "Isles of Scilly");
			this.areaCodes.put("1721", "Peebles");
			this.areaCodes.put("1722", "Salisbury");
			this.areaCodes.put("1723", "Scarborough");
			this.areaCodes.put("1724", "Scunthorpe");
			this.areaCodes.put("1725", "Rockbourne");
			this.areaCodes.put("1726", "St Austell");
			this.areaCodes.put("1727", "St Albans");
			this.areaCodes.put("1728", "Saxmundham");
			this.areaCodes.put("1729", "Settle, Ribblesdale");
			this.areaCodes.put("1730", "Petersfield");
			this.areaCodes.put("1732", "Sevenoaks");
			this.areaCodes.put("1733", "Peterborough");
			this.areaCodes.put("1736", "Penzance");
			this.areaCodes.put("1737", "Redhill");
			this.areaCodes.put("1738", "Perth");
			this.areaCodes.put("1740", "Sedgefield");
			this.areaCodes.put("1743", "Shrewsbury");
			this.areaCodes.put("1744", "St Helens");
			this.areaCodes.put("1745", "Rhyl");
			this.areaCodes.put("1746", "Bridgnorth, Shropshire");
			this.areaCodes.put("1747", "Shaftesbury");
			this.areaCodes.put("1748", "Richmond");
			this.areaCodes.put("1749", "Shepton Mallet");
			this.areaCodes.put("1750", "Selkirk");
			this.areaCodes.put("1751", "Pickering");
			this.areaCodes.put("1752", "Plymouth");
			this.areaCodes.put("1753", "Slough");
			this.areaCodes.put("1754", "Skegness");
			this.areaCodes.put("1756", "Skipton");
			this.areaCodes.put("1757", "Selby");
			this.areaCodes.put("1758", "Pwllheli");
			this.areaCodes.put("1759", "Pocklington");
			this.areaCodes.put("1760", "Swaffham");
			this.areaCodes.put("1761", "Temple Cloud, Somerset");
			this.areaCodes.put("1763", "Royston");
			this.areaCodes.put("1764", "Crieff, Ruthven");
			this.areaCodes.put("1765", "Ripon");
			this.areaCodes.put("1766", "Porthmadog");
			this.areaCodes.put("1767", "Sandy");
			this.areaCodes.put("1768", "Penrith");
			this.areaCodes.put("17683", "Appleby");
			this.areaCodes.put("17684", "Pooley Bridge");
			this.areaCodes.put("17687", "Keswick");
			this.areaCodes.put("1769", "South Molton");
			this.areaCodes.put("1770", "Isle of Arran");
			this.areaCodes.put("1771", "Maud");
			this.areaCodes.put("1772", "Preston");
			this.areaCodes.put("1773", "Ripley");
			this.areaCodes.put("1775", "Spalding");
			this.areaCodes.put("1776", "Stranraer");
			this.areaCodes.put("1777", "Retford");
			this.areaCodes.put("1778", "Market Deeping/Bourne");
			this.areaCodes.put("1779", "Peterhead");
			this.areaCodes.put("1780", "Stamford");
			this.areaCodes.put("1782", "Stoke-on-Trent");
			this.areaCodes.put("1784", "Staines");
			this.areaCodes.put("1785", "Stafford");
			this.areaCodes.put("1786", "Stirling");
			this.areaCodes.put("1787", "Sudbury");
			this.areaCodes.put("1788", "Rugby");
			this.areaCodes.put("1789", "Stratford-upon-Avon");
			this.areaCodes.put("1790", "Spilsby");
			this.areaCodes.put("1792", "Swansea");
			this.areaCodes.put("1793", "Swindon");
			this.areaCodes.put("1794", "Romsey");
			this.areaCodes.put("1795", "Sittingbourne, Sheppey");
			this.areaCodes.put("1796", "Pitlochry");
			this.areaCodes.put("1797", "Rye");
			this.areaCodes.put("1798", "Pulborough, Sussex");
			this.areaCodes.put("1799", "Saffron Walden");
			this.areaCodes.put("1803", "Torquay");
			this.areaCodes.put("1805", "Torrington");
			this.areaCodes.put("1806", "Voe, Shetland");
			this.areaCodes.put("1807", "Ballindalloch, Tomintoul");
			this.areaCodes.put("1808", "Tomatin");
			this.areaCodes.put("1809", "Tomdoun");
			this.areaCodes.put("1821", "Kinrossie, Tayside");
			this.areaCodes.put("1822", "Tavistock");
			this.areaCodes.put("1823", "Taunton");
			this.areaCodes.put("1824", "Ruthin, Vale of Clwyd");
			this.areaCodes.put("1825", "Uckfield");
			this.areaCodes.put("1827", "Tamworth");
			this.areaCodes.put("1828", "Coupar Angus, Tayside");
			this.areaCodes.put("1829", "Tarporley");
			this.areaCodes.put("1830", "Kirkwhelpington");
			this.areaCodes.put("1832", "Clopton, Oundle");
			this.areaCodes.put("1833", "Barnard Castle, Teesdale");
			this.areaCodes.put("1834", "Narberth, Tenby");
			this.areaCodes.put("1835", "St Boswells");
			this.areaCodes.put("1837", "Okehampton");
			this.areaCodes.put("1838", "Dalmally, Tyndrum");
			this.areaCodes.put("1840", "Camelford, Tintagel");
			this.areaCodes.put("1841", "Padstow, Newquay");
			this.areaCodes.put("1842", "Thetford");
			this.areaCodes.put("1843", "Thanet");
			this.areaCodes.put("1844", "Thame");
			this.areaCodes.put("1845", "Thirsk");
			this.areaCodes.put("1847", "Thurso and Tongue");
			this.areaCodes.put("1848", "Thornhill");
			this.areaCodes.put("1851", "Great Bernera and Stornoway");
			this.areaCodes.put("1852", "Kilmelford");
			this.areaCodes.put("1854", "Ullapool");
			this.areaCodes.put("1855", "Ballachulish");
			this.areaCodes.put("1856", "Orkney");
			this.areaCodes.put("1857", "Sanday");
			this.areaCodes.put("1858", "Market Harborough");
			this.areaCodes.put("1859", "Harris");
			this.areaCodes.put("1862", "Tain");
			this.areaCodes.put("1863", "Ardgay, Tain");
			this.areaCodes.put("1864", "Tinto, Abington, Crawford");
			this.areaCodes.put("1865", "Oxford");
			this.areaCodes.put("1866", "Kilchrenan");
			this.areaCodes.put("1869", "Bicester, Oxfordshire");
			this.areaCodes.put("1870", "Isle of Benbecula");
			this.areaCodes.put("1871", "Castlebay");
			this.areaCodes.put("1872", "Truro");
			this.areaCodes.put("1873", "Abergavenny, Usk");
			this.areaCodes.put("1874", "Brecon, Usk");
			this.areaCodes.put("1875", "Tranent");
			this.areaCodes.put("1876", "Lochmaddy");
			this.areaCodes.put("1877", "Callander, Trossachs");
			this.areaCodes.put("1878", "Lochboisdale");
			this.areaCodes.put("1879", "Scarinish, Tiree");
			this.areaCodes.put("1880", "Tarbert");
			this.areaCodes.put("1882", "Kinloch Rannoch, Tummel Bridge");
			this.areaCodes.put("1883", "Caterham");
			this.areaCodes.put("1884", "Tiverton");
			this.areaCodes.put("1885", "Pencombe");
			this.areaCodes.put("1886", "Bromyard, Teme Valley");
			this.areaCodes.put("1887", "Aberfeldy, Tay Valley");
			this.areaCodes.put("1888", "Turriff");
			this.areaCodes.put("1889", "Rugeley, Uttoxeter");
			this.areaCodes.put("1890", "Ayton, Berwickshire and Coldstream, Tweed");
			this.areaCodes.put("1892", "Tunbridge Wells");
			this.areaCodes.put("1895", "Uxbridge");
			this.areaCodes.put("1896", "Galashiels, Tweed");
			this.areaCodes.put("1899", "Biggar, Tweed");
			this.areaCodes.put("1900", "Workington");
			this.areaCodes.put("1902", "Wolverhampton");
			this.areaCodes.put("1903", "Worthing");
			this.areaCodes.put("1904", "York");
			this.areaCodes.put("1905", "Worcester");
			this.areaCodes.put("1908", "Milton Keynes, Wolverton");
			this.areaCodes.put("1909", "Worksop");
			this.areaCodes.put("1920", "Ware");
			this.areaCodes.put("1922", "Walsall");
			this.areaCodes.put("1923", "Watford");
			this.areaCodes.put("1924", "Wakefield");
			this.areaCodes.put("1925", "Warrington");
			this.areaCodes.put("1926", "Warwick");
			this.areaCodes.put("1928", "Runcorn, Warrington");
			this.areaCodes.put("1929", "Wareham");
			this.areaCodes.put("1931", "Shap, Westmorland");
			this.areaCodes.put("1932", "Weybridge");
			this.areaCodes.put("1933", "Wellingborough");
			this.areaCodes.put("1934", "Weston-super-Mare");
			this.areaCodes.put("1935", "Yeovil");
			this.areaCodes.put("1937", "Wetherby");
			this.areaCodes.put("1938", "Welshpool");
			this.areaCodes.put("1939", "Wem");
			this.areaCodes.put("1942", "Wigan");
			this.areaCodes.put("1943", "Guiseley, Wharfedale");
			this.areaCodes.put("1944", "West Heslerton");
			this.areaCodes.put("1945", "Wisbech");
			this.areaCodes.put("1946", "Whitehaven");
			this.areaCodes.put("19467", "Gosforth");
			this.areaCodes.put("1947", "Whitby");
			this.areaCodes.put("1948", "Whitchurch");
			this.areaCodes.put("1949", "Whatton");
			this.areaCodes.put("1950", "Sandwick, Yell");
			this.areaCodes.put("1951", "Colonsay");
			this.areaCodes.put("1952", "Telford, Wellington");
			this.areaCodes.put("1953", "Wymondham");
			this.areaCodes.put("1954", "Madingley, Willingham");
			this.areaCodes.put("1955", "Wick");
			this.areaCodes.put("1957", "Mid Yell, Yell");
			this.areaCodes.put("1959", "Westerham, West Kent");
			this.areaCodes.put("1962", "Winchester");
			this.areaCodes.put("1963", "Wincanton");
			this.areaCodes.put("1964", "Hornsea and Patrington, Withernsea");
			this.areaCodes.put("1967", "Strontian");
			this.areaCodes.put("1968", "Penicuik, West Linton");
			this.areaCodes.put("1969", "Leyburn, Wensleydale");
			this.areaCodes.put("1970", "Aberystwyth, Ystwyth");
			this.areaCodes.put("1971", "Scourie, Wrath");
			this.areaCodes.put("1972", "Glenborrodale");
			this.areaCodes.put("1974", "Llanon, Ystwyth");
			this.areaCodes.put("1975", "Alford, Aberdeenshire and Strathdon, Water");
			this.areaCodes.put("1977", "Pontefract, West Riding");
			this.areaCodes.put("1978", "Wrexham");
			this.areaCodes.put("1980", "Amesbury, Wiltshire");
			this.areaCodes.put("1981", "Wormbridge");
			this.areaCodes.put("1982", "Builth Wells");
			this.areaCodes.put("1983", "Isle of Wight");
			this.areaCodes.put("1984", "Watchet, Williton");
			this.areaCodes.put("1985", "Warminster");
			this.areaCodes.put("1986", "Bungay, Waveney");
			this.areaCodes.put("1987", "Ebbsfleet");
			this.areaCodes.put("1988", "Wigtown");
			this.areaCodes.put("1989", "Ross-on-Wye, Wye");
			this.areaCodes.put("1992", "Lea Valley, Waltham X");
			this.areaCodes.put("1993", "Witney");
			this.areaCodes.put("1994", "St Clears, West Wales");
			this.areaCodes.put("1995", "Garstang, Wyre");
			this.areaCodes.put("1997", "Strathpeffer, Wyvis");
			this.areaCodes.put("30", "Non-geographic (charged at 01/02 rate)");
			this.areaCodes.put("33", "Non-geographic (charged at 01/02 rate)");
			this.areaCodes.put("34", "Non-geographic (charged at 01/02 rate)");
			this.areaCodes.put("37", "Non-geographic (charged at 01/02 rate)");
			this.areaCodes.put("500", "Freephone");
			this.areaCodes.put("55", "Voice over IP");
			this.areaCodes.put("56", "Voice over IP");
			this.areaCodes.put("70", "Personal number (Premium rate)");
			this.areaCodes.put("74", "Mobile");
			this.areaCodes.put("75", "Mobile");
			this.areaCodes.put("76", "Pager");
			this.areaCodes.put("7624", "Mobile");
			this.areaCodes.put("77", "Mobile");
			this.areaCodes.put("78", "Mobile");
			this.areaCodes.put("79", "Mobile");
			this.areaCodes.put("800", "Freephone");
			this.areaCodes.put("808", "Freephone");
			this.areaCodes.put("842", "Non-geographic/special");
			this.areaCodes.put("843", "Non-geographic/special");
			this.areaCodes.put("844", "Non-geographic/special");
			this.areaCodes.put("845", "Non-geographic");
			this.areaCodes.put("870", "Non-geographic/special");
			this.areaCodes.put("871", "Premium rate");
			this.areaCodes.put("872", "Premium rate");
			this.areaCodes.put("873", "Premium rate");
			this.areaCodes.put("90", "Premium rate");
			this.areaCodes.put("91", "Premium rate");
			this.areaCodes.put("98", "Premium rate");
		}
		phoneNumber = phoneNumber.replaceAll("[\\(\\)\\s]", "");
		phoneNumber = phoneNumber.replaceAll("(?:(?:0(?:0|11)\\s?|\\+)4\\s?4)?0?([1-9]\\d+)[x\\#]?.*",
				"$1");
		// stop substring creating an IndexOutOfBoundsException below
		if (phoneNumber.length() < 6) {
			return "";
		}
		for (int numDigitsInCode = 5; numDigitsInCode > 1; numDigitsInCode--) {
			String possibleCode = phoneNumber.substring(0, numDigitsInCode);
			String area = this.areaCodes.get(possibleCode);
			if (area != null) {
				return area;
			}
		}
		return "(may be invalid)";
	}

	public String getCountryForPhoneNumber(String phoneNumber) {
		if (this.countryCodes.isEmpty()) {
			this.countryCodes.put("1", "Canada and United States");

			/* 1... */
			this.countryCodes.put("1340", "United States Virgin Islands");
			this.countryCodes.put("1670", "Northern Mariana Islands");
			this.countryCodes.put("1671", "Guam");
			this.countryCodes.put("1684", "American Samoa");
			this.countryCodes.put("1787", "Puerto Rico");
			this.countryCodes.put("1939", "Puerto Rico");
			this.countryCodes.put("1242", "Bahamas");
			this.countryCodes.put("1246", "Barbados");
			this.countryCodes.put("1264", "Anguilla");
			this.countryCodes.put("1268", "Antigua and Barbuda");
			this.countryCodes.put("1284", "British Virgin Islands");
			this.countryCodes.put("1345", "Cayman Islands");
			this.countryCodes.put("1441", "Bermuda");
			this.countryCodes.put("1473", "Grenada");
			this.countryCodes.put("1649", "Turks and Caicos Islands");
			this.countryCodes.put("1664", "Montserrat");
			this.countryCodes.put("1721", "Sint Maarten");
			this.countryCodes.put("1758", "Saint Lucia");
			this.countryCodes.put("1767", "Dominica");
			this.countryCodes.put("1784", "Saint Vincent and the Grenadines");
			this.countryCodes.put("1809", "Dominican Republic");
			this.countryCodes.put("1829", "Dominican Republic");
			this.countryCodes.put("1849", "Dominican Republic");
			this.countryCodes.put("1868", "Trinidad and Tobago");
			this.countryCodes.put("1869", "Saint Kitts and Nevis");
			this.countryCodes.put("1876", "Jamaica");

			this.countryCodes.put("20", "Egypt");
			this.countryCodes.put("210", "unassigned");
			this.countryCodes.put("211", "South Sudan");
			this.countryCodes.put("212", "Morocco");

			/* 212... */
			this.countryCodes.put("2125288", "Western Sahara");
			this.countryCodes.put("2125289", "Western Sahara");

			this.countryCodes.put("213", "Algeria");
			this.countryCodes.put("214", "unassigned");
			this.countryCodes.put("215", "unassigned");
			this.countryCodes.put("216", "Tunisia");
			this.countryCodes.put("217", "unassigned");
			this.countryCodes.put("218", "Libya");
			this.countryCodes.put("219", "unassigned");
			this.countryCodes.put("220", "Gambia");
			this.countryCodes.put("221", "Senegal");
			this.countryCodes.put("222", "Mauritania");
			this.countryCodes.put("223", "Mali");
			this.countryCodes.put("224", "Guinea");
			this.countryCodes.put("225", "Ivory Coast");
			this.countryCodes.put("226", "Burkina Faso");
			this.countryCodes.put("227", "Niger");
			this.countryCodes.put("228", "Togo");
			this.countryCodes.put("229", "Benin");
			this.countryCodes.put("230", "Mauritius");
			this.countryCodes.put("231", "Liberia");
			this.countryCodes.put("232", "Sierra Leone");
			this.countryCodes.put("233", "Ghana");
			this.countryCodes.put("234", "Nigeria");
			this.countryCodes.put("235", "Chad");
			this.countryCodes.put("236", "Central African Republic");
			this.countryCodes.put("237", "Cameroon");
			this.countryCodes.put("238", "Cape Verde");
			this.countryCodes.put("239", "Sao Tome and Principe");
			this.countryCodes.put("240", "Equatorial Guinea");
			this.countryCodes.put("241", "Gabon");
			this.countryCodes.put("242", "Republic of the Congo");
			this.countryCodes.put("243", "Democratic Republic of the Congo");
			this.countryCodes.put("244", "Angola");
			this.countryCodes.put("245", "Guinea-Bissau");
			this.countryCodes.put("246", "British Indian Ocean Territory");
			this.countryCodes.put("247", "Ascension Island");
			this.countryCodes.put("248", "Seychelles");
			this.countryCodes.put("249", "Sudan");
			this.countryCodes.put("250", "Rwanda");
			this.countryCodes.put("251", "Ethiopia");
			this.countryCodes.put("252", "Somalia");
			this.countryCodes.put("253", "Djibouti");
			this.countryCodes.put("254", "Kenya");
			this.countryCodes.put("255", "Tanzania");

			/* 255... */
			this.countryCodes.put("25524", "Zanzibar");

			this.countryCodes.put("256", "Uganda");
			this.countryCodes.put("257", "Burundi");
			this.countryCodes.put("258", "Mozambique");
			this.countryCodes.put("259", "unassigned");
			this.countryCodes.put("260", "Zambia");
			this.countryCodes.put("261", "Madagascar");
			this.countryCodes.put("262", "Reunion");

			/* 262... */
			this.countryCodes.put("262269", "Mayotte");
			this.countryCodes.put("262639", "Mayotte");

			this.countryCodes.put("263", "Zimbabwe");
			this.countryCodes.put("264", "Namibia");
			this.countryCodes.put("265", "Malawi");
			this.countryCodes.put("266", "Lesotho");
			this.countryCodes.put("267", "Botswana");
			this.countryCodes.put("268", "Swaziland");
			this.countryCodes.put("269", "Comoros");
			this.countryCodes.put("27", "South Africa");
			this.countryCodes.put("28", "unassigned");
			this.countryCodes.put("290", "Saint Helena");

			/* 290... */
			this.countryCodes.put("2908", "Tristan da Cunha");

			this.countryCodes.put("291", "Eritrea");
			this.countryCodes.put("292", "unassigned");
			this.countryCodes.put("293", "unassigned");
			this.countryCodes.put("294", "unassigned");
			this.countryCodes.put("295", "discontinued");
			this.countryCodes.put("296", "unassigned");
			this.countryCodes.put("297", "Aruba");
			this.countryCodes.put("298", "Faroe Islands");
			this.countryCodes.put("299", "Greenland");
			this.countryCodes.put("30", "Greece");
			this.countryCodes.put("31", "Netherlands");
			this.countryCodes.put("32", "Belgium");
			this.countryCodes.put("33", "France");
			this.countryCodes.put("34", "Spain");
			this.countryCodes.put("350", "Gibraltar");
			this.countryCodes.put("351", "Portugal");
			this.countryCodes.put("352", "Luxembourg");
			this.countryCodes.put("353", "Ireland");
			this.countryCodes.put("354", "Iceland");
			this.countryCodes.put("355", "Albania");
			this.countryCodes.put("356", "Malta");
			this.countryCodes.put("357", "Cyprus");
			this.countryCodes.put("358", "Finland");

			/* 358... */
			this.countryCodes.put("35818", "Aland Island");

			this.countryCodes.put("359", "Bulgaria");
			this.countryCodes.put("36", "Hungary");
			this.countryCodes.put("370", "Lithuania");
			this.countryCodes.put("371", "Latvia");
			this.countryCodes.put("372", "Estonia");
			this.countryCodes.put("373", "Moldova");

			/* 373... */
			this.countryCodes.put("3732", "Transnistria");
			this.countryCodes.put("3735", "Transnistria");

			this.countryCodes.put("374", "Armenia");

			/* 374... */
			this.countryCodes.put("37447", "Nagorno-Karabakh");
			this.countryCodes.put("37497", "Nagorno-Karabakh");

			this.countryCodes.put("375", "Belarus");
			this.countryCodes.put("376", "Andorra");
			this.countryCodes.put("377", "Monaco");

			/* 377... */
			this.countryCodes.put("37745", "Kosovo");
			this.countryCodes.put("37745", "Kosovo");

			this.countryCodes.put("378", "San Marino");
			this.countryCodes.put("379", "Vatican City");
			this.countryCodes.put("380", "Ukraine");
			this.countryCodes.put("381", "Serbia");

			/* 381... */
			this.countryCodes.put("38128", "Kosovo");
			this.countryCodes.put("38129", "Kosovo");
			this.countryCodes.put("38138", "Kosovo");
			this.countryCodes.put("38139", "Kosovo");

			this.countryCodes.put("382", "Montenegro");
			this.countryCodes.put("383", "unassigned");
			this.countryCodes.put("384", "unassigned");
			this.countryCodes.put("385", "Croatia");
			this.countryCodes.put("386", "Slovenia");

			/* 386... */
			this.countryCodes.put("38643", "Kosovo");
			this.countryCodes.put("38649", "Kosovo");

			this.countryCodes.put("387", "Bosnia and Herzegovina");
			this.countryCodes.put("388", "discontinued");
			this.countryCodes.put("389", "Macedonia");
			this.countryCodes.put("39", "Italy");

			/* 39... */
			this.countryCodes.put("3906698", "Vatican City");

			this.countryCodes.put("40", "Romania");
			this.countryCodes.put("41", "Switzerland");
			this.countryCodes.put("420", "Czech Republic");
			this.countryCodes.put("421", "Slovakia");
			this.countryCodes.put("422", "unassigned");
			this.countryCodes.put("423", "Liechtenstein");
			this.countryCodes.put("424", "unassigned");
			this.countryCodes.put("425", "unassigned");
			this.countryCodes.put("426", "unassigned");
			this.countryCodes.put("427", "unassigned");
			this.countryCodes.put("428", "unassigned");
			this.countryCodes.put("429", "unassigned");
			this.countryCodes.put("43", "Austria");
			this.countryCodes.put("45", "Denmark");
			this.countryCodes.put("46", "Sweden");
			this.countryCodes.put("47", "Norway");

			/* 47... */
			this.countryCodes.put("4779", "Svalbard and Jan Mayen");

			this.countryCodes.put("48", "Poland");
			this.countryCodes.put("49", "Germany");
			this.countryCodes.put("500", "Falkland Islands");
			this.countryCodes.put("501", "Belize");
			this.countryCodes.put("502", "Guatemala");
			this.countryCodes.put("503", "El Salvador");
			this.countryCodes.put("504", "Honduras");
			this.countryCodes.put("505", "Nicaragua");
			this.countryCodes.put("506", "Costa Rica");
			this.countryCodes.put("507", "Panama");
			this.countryCodes.put("508", "Saint-Pierre and Miquelon");
			this.countryCodes.put("509", "Haiti");
			this.countryCodes.put("51", "Peru");
			this.countryCodes.put("52", "Mexico");
			this.countryCodes.put("53", "Cuba");
			this.countryCodes.put("54", "Argentina");
			this.countryCodes.put("55", "Brazil");
			this.countryCodes.put("56", "Chile");

			/* 56... */
			this.countryCodes.put("5632", "Easter Island");

			this.countryCodes.put("57", "Colombia");
			this.countryCodes.put("58", "Venezuela");
			this.countryCodes.put("590", "Guadeloupe (including Saint Barthelemy, Saint Martin)");
			this.countryCodes.put("591", "Bolivia");
			this.countryCodes.put("592", "Guyana");
			this.countryCodes.put("593", "Ecuador");
			this.countryCodes.put("594", "French Guiana");
			this.countryCodes.put("595", "Paraguay");
			this.countryCodes.put("596", "Martinique");
			this.countryCodes.put("597", "Suriname");
			this.countryCodes.put("598", "Uruguay");

			/* 599.. */
			this.countryCodes.put("5993", "Sint Eustatius");
			this.countryCodes.put("5994", "Saba");
			this.countryCodes.put("5995", "formerly Sint Maarten");
			this.countryCodes.put("5997", "Bonaire");
			this.countryCodes.put("5999", "Curacao");

			this.countryCodes.put("60", "Malaysia");
			this.countryCodes.put("61", "Australia");

			/* 61... */
			this.countryCodes.put("6189162", "Cocos Islands");
			this.countryCodes.put("6189164", "Christmas Island");

			this.countryCodes.put("62", "Indonesia");
			this.countryCodes.put("63", "Philippines");
			this.countryCodes.put("64", "New Zealand");
			this.countryCodes.put("65", "Singapore");
			this.countryCodes.put("66", "Thailand");
			this.countryCodes.put("670", "East Timor");
			this.countryCodes.put("671", "formerly Guam");
			this.countryCodes.put("672", "Australian External Territories");

			/* 672... */
			this.countryCodes.put("6721", "Australia Australian Antarctic Territory");
			this.countryCodes.put("6723", "Norfolk Island");

			this.countryCodes.put("673", "Brunei");
			this.countryCodes.put("674", "Nauru");
			this.countryCodes.put("675", "Papua New Guinea");
			this.countryCodes.put("676", "Tonga");
			this.countryCodes.put("677", "Solomon Islands");
			this.countryCodes.put("678", "Vanuatu");
			this.countryCodes.put("679", "Fiji");
			this.countryCodes.put("680", "Palau");
			this.countryCodes.put("681", "Wallis and Futuna");
			this.countryCodes.put("682", "Cook Islands");
			this.countryCodes.put("683", "Niue");
			this.countryCodes.put("684", "formerly American Samoa");
			this.countryCodes.put("685", "Samoa");
			this.countryCodes.put("686", "Kiribati");
			this.countryCodes.put("687", "New Caledonia");
			this.countryCodes.put("688", "Tuvalu");
			this.countryCodes.put("689", "French Polynesia");
			this.countryCodes.put("690", "Tokelau");
			this.countryCodes.put("691", "Federated States of Micronesia");
			this.countryCodes.put("692", "Marshall Islands");
			this.countryCodes.put("693", "unassigned");
			this.countryCodes.put("694", "unassigned");
			this.countryCodes.put("695", "unassigned");
			this.countryCodes.put("696", "unassigned");
			this.countryCodes.put("697", "unassigned");
			this.countryCodes.put("698", "unassigned");
			this.countryCodes.put("699", "unassigned");
			this.countryCodes.put("7", "Russia");
			this.countryCodes.put("76", "Kazakhstan");
			this.countryCodes.put("77", "Kazakhstan");
			this.countryCodes.put("7840", "Abkhazia");
			this.countryCodes.put("7940", "Abkhazia");
			this.countryCodes.put("800", "International Freephone (UIFN)");
			this.countryCodes.put("801", "unassigned");
			this.countryCodes.put("802", "unassigned");
			this.countryCodes.put("803", "unassigned");
			this.countryCodes.put("804", "unassigned");
			this.countryCodes.put("805", "unassigned");
			this.countryCodes.put("806", "unassigned");
			this.countryCodes.put("807", "unassigned");
			this.countryCodes.put("808", "reserved for Shared Cost Services");
			this.countryCodes.put("809", "unassigned");
			this.countryCodes.put("81", "Japan");
			this.countryCodes.put("82", "South Korea");
			this.countryCodes.put("83", "unassigned");
			this.countryCodes.put("84", "Vietnam");
			this.countryCodes.put("850", "North Korea");
			this.countryCodes.put("851", "unassigned");
			this.countryCodes.put("852", "Hong Kong");
			this.countryCodes.put("853", "Macau");
			this.countryCodes.put("854", "unassigned");
			this.countryCodes.put("855", "Cambodia");
			this.countryCodes.put("856", "Laos");
			this.countryCodes.put("857", "unassigned, formerly ANAC satellite service");
			this.countryCodes.put("858", "unassigned, formerly ANAC satellite service");
			this.countryCodes.put("859", "unassigned");
			this.countryCodes.put("86", "China");
			this.countryCodes.put("870", "Inmarsat 'SNAC' service");
			this.countryCodes.put("871",
					"unassigned (formerly used by Inmarsat, Atlantic East), discontinued in 2008");
			this.countryCodes.put("872",
					"unassigned (formerly used by Inmarsat, Pacific), discontinued in 2008");
			this.countryCodes.put("873",
					"unassigned (formerly used by Inmarsat, Indian), discontinued in 2008");
			this.countryCodes.put("874",
					"unassigned (formerly used by Inmarsat, Atlantic West), discontinued 2008");
			this.countryCodes.put("875", "reserved for Maritime Mobile service");
			this.countryCodes.put("876", "reserved for Maritime Mobile service");
			this.countryCodes.put("877", "reserved for Maritime Mobile service");
			this.countryCodes.put("878", "Universal Personal Telecommunications services");
			this.countryCodes.put("879", "reserved for national non-commercial purposes");
			this.countryCodes.put("880", "Bangladesh");
			this.countryCodes.put("881", "Global Mobile Satellite System");
			this.countryCodes.put("882", "International Networks");
			this.countryCodes.put("883", "International Networks");
			this.countryCodes.put("884", "unassigned");
			this.countryCodes.put("885", "unassigned");
			this.countryCodes.put("886", "Taiwan");
			this.countryCodes.put("887", "unassigned");
			this.countryCodes.put("888", "Telecommunications for Disaster Relief by OCHA");
			this.countryCodes.put("889", "unassigned");
			this.countryCodes.put("89", "unassigned");
			this.countryCodes.put("90", "Turkey");

			/* 90... */
			this.countryCodes.put("90392", "Northern Cyprus");

			this.countryCodes.put("91", "India");
			this.countryCodes.put("92", "Pakistan");
			this.countryCodes.put("93", "Afghanistan");
			this.countryCodes.put("94", "Sri Lanka");
			this.countryCodes.put("95", "Burma");
			this.countryCodes.put("960", "Maldives");
			this.countryCodes.put("961", "Lebanon");
			this.countryCodes.put("962", "Jordan");
			this.countryCodes.put("963", "Syria");
			this.countryCodes.put("964", "Iraq");
			this.countryCodes.put("965", "Kuwait");
			this.countryCodes.put("966", "Saudi Arabia");
			this.countryCodes.put("967", "Yemen");
			this.countryCodes.put("968", "Oman");
			this.countryCodes.put("969", "unassigned");
			this.countryCodes.put("970", "Palestinian territories");
			this.countryCodes.put("971", "United Arab Emirates");
			this.countryCodes.put("972", "Israel");
			this.countryCodes.put("973", "Bahrain");
			this.countryCodes.put("974", "Qatar");
			this.countryCodes.put("975", "Bhutan");
			this.countryCodes.put("976", "Mongolia");
			this.countryCodes.put("977", "Nepal");
			this.countryCodes.put("978", "unassigned");
			this.countryCodes.put("979", "International Premium Rate Service");
			this.countryCodes.put("98", "Iran");
			this.countryCodes.put("990", "unassigned");
			this.countryCodes.put("991",
					"International Telecommunications Public Correspondence Service trial (ITPCS)");
			this.countryCodes.put("992", "Tajikistan");
			this.countryCodes.put("993", "Turkmenistan");
			this.countryCodes.put("994", "Azerbaijan");
			this.countryCodes.put("995", "Georgia");

			/* 995... */
			this.countryCodes.put("99534", "South Ossetia");
			this.countryCodes.put("99544", "Abkhazia");

			this.countryCodes.put("996", "Kyrgyzstan");
			this.countryCodes.put("997", "unassigned");
			this.countryCodes.put("998", "Uzbekistan");
			this.countryCodes.put("999", "reserved for future global service");
		}
		phoneNumber = phoneNumber.replaceAll("[\\(\\)]", "");
		phoneNumber = phoneNumber
				.replaceAll("(?:00\\s?|\\+)([1-9][\\d\\s]+)[x\\#]?.*", "$1");
		phoneNumber = phoneNumber.replaceAll("[\\s]", "");
		// stop substring creating an IndexOutOfBoundsException below
		if (phoneNumber.length() < 8) {
			return "";
		}
		// Known issue: Numbers as short as +290 5555 fail to show country name
		for (int numDigitsInCountry = 7; numDigitsInCountry > 0; numDigitsInCountry--) {
			String possibleCountry = phoneNumber.substring(0, numDigitsInCountry);
			String country = this.countryCodes.get(possibleCountry);
			if (country != null) {
				return country;
			}
		}
		return "(country code may be invalid)";
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

	public Set<FieldTypeDescriptorInfo> getFieldTypeDescriptors() throws ObjectNotFoundException {
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
		logger.info("Template message at " + System.currentTimeMillis() + "("
				+ this.request.getRemoteUser() + ") : " + itemToLog);
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
			// URLEncoder.encode replaces spaces with plus signs which is not
			// what we want. We want to keep newlines too
			encoded = string.replaceAll("\\s", "gtpb_special_variable_space");
			encoded = encoded.replaceAll("\\n", "gtpb_special_variable_newline");
			if (encoded.contains("/")) {
				// Only encode content after the path
				String filename = encoded.replaceAll("^.*\\/", "");
				String path = encoded.substring(0, encoded.length() - filename.length());
				encoded = path + java.net.URLEncoder.encode(filename, "UTF-8");
			} else {
				encoded = java.net.URLEncoder.encode(encoded, "UTF-8");
			}
			encoded = encoded.replace("gtpb_special_variable_space", "%20");
			encoded = encoded.replace("gtpb_special_variable_newline", "\n");
		} catch (UnsupportedEncodingException ueex) {
			logger.error("Error URL encoding string '" + string + "': " + ueex);
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
		if (browsersMatched.size() > 0) {
			// Return the last (most specific) browser in the list of matches
			LinkedList<Browsers> br = new LinkedList<Browsers>(browsersMatched);
			return br.getLast();
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
		return new TreeSet<String>();
	}

	public Set<TableInfo> getNewTableSet() {
		return new TreeSet<TableInfo>();
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
		return RandomString.generate();
	}

	public String getAppUrl() {
		return Helpers.getAppUrl(this.request);
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
		if (stringToConvert == null) {
			return "";
		}
		return stringToConvert.replaceAll("\n", "<p>");
	}

	public String unencodeHtml(String string) {
		return Helpers.unencodeHtml(string);
	}

	public synchronized boolean templateExists(String templateFilename) {
		Boolean templateExists = this.templateExistsCache.get(templateFilename);
		if (templateExists != null) {
			return templateExists;
		}
		String absoluteFilename = this.request.getSession().getServletContext()
				.getRealPath("/WEB-INF/templates/" + templateFilename);
		File templateFile = new File(absoluteFilename);
		templateExists = templateFile.exists();
		this.templateExistsCache.put(templateFilename, templateExists);
		return templateExists;
	}

	public Set<File> listFiles(String folderName) {
		Set<File> files = null;
		String absoluteFolderName = this.request.getSession().getServletContext()
				.getRealPath("/" + folderName);
		File folder = new File(absoluteFolderName);
		File[] filesArray = folder.listFiles();
		if (filesArray != null) {
			files = new TreeSet<File>(Arrays.asList(filesArray));
		} else {
			files = new TreeSet<File>();
		}
		return files;
	}

	public String getExternalHtml(String url) throws ClientProtocolException, IOException {
		logger.debug("Getting external URL " + url);
		return Request.Get(url).execute().returnContent().asString();
	}

	public String getCommitUrl() throws IOException, CantDoThatException {
		String commitFileName = this.request.getSession().getServletContext()
				.getRealPath("/lastcommit.txt");
		File commitFile = new File(commitFileName);
		try {
			InputStream inputStream = new FileInputStream(commitFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
					Charset.forName("UTF-8")));
			String commitLine = reader.readLine();
			String commitId = commitLine.replace("commit ", "");
			inputStream.close();
			return "https://github.com/okohll/agileBase/commit/" + commitId;
		} catch (FileNotFoundException fnfex) {
			logger.error("Commit file " + commitFileName + " not found: " + fnfex);
			// Throw exception but don't show the actual file path
			throw new CantDoThatException("Commit log not found");
		}
	}

	public String getCommitMessage() throws CantDoThatException, IOException {
		String commitFileName = this.request.getSession().getServletContext()
				.getRealPath("/lastcommit.txt");
		File commitFile = new File(commitFileName);
		try {
			InputStream inputStream = new FileInputStream(commitFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
					Charset.forName("UTF-8")));
			String line = null;
			String message = "";
			while ((line = reader.readLine()) != null) {
				message += line + "<br />";
			}
			inputStream.close();
			return message;
		} catch (FileNotFoundException fnfex) {
			logger.error("Commit file " + commitFileName + " not found: " + fnfex);
			// Throw exception but don't show the actual file path
			throw new CantDoThatException("Commit log not found");
		}
	}

	public String toString() {
		return "ViewTools contains utility methods useful to Velocity template designers";
	}

	public void throwException() throws CantDoThatException {
		throw new CantDoThatException("Test error message");
	}

	private final HttpServletRequest request;

	private final HttpServletResponse response;

	private final String webAppRoot;

	private MathTool mathTool = new MathTool();

	private Map<String, BigInteger> timers = new HashMap<String, BigInteger>();

	/**
	 * A map of telephone area code to city / location
	 * 
	 * Don't need ConcurrentHashMap here as data will only be put into this
	 * cache once
	 */
	private Map<String, String> areaCodes = new HashMap<String, String>(1000);

	private Map<String, String> countryCodes = new HashMap<String, String>();

	private Map<String, Boolean> templateExistsCache = new HashMap<String, Boolean>();

	private static final SimpleLogger logger = new SimpleLogger(ViewTools.class);


}