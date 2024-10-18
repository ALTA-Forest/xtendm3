// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-09-05
// @version   1.0 
//
// Description 
// This API is to list log headers from EXTSLH and EXTSLD 
// Transaction LstLogByDeck
// AFMI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * 
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: TDCK - To Deck
 * @return: SPEC - Species
 * @return: LLEN - Length
 * @return: LGRV - Gross Volume
 * @return: LNEV - Net Volume
 * @return: LAMT - Amount
 * 
*/

public class LstLogByDeck extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database  
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inDLNO
  double length
  double grossVolume
  double netVolume
  double sumLength
  double sumGrossVolume
  double sumNetVolume
  double amount
  int copyCONO
  String copyDIVI
  int copyTDCK
  int copySTID
  int copyLGID
  double copyLAMT
  String copySPEC
  double copyLLEN
  double copyLGRV
  double copyLNEV
  double copyLEND
  double copyLSDI
  double copyLLDI
  double copyDIAD
  String copyGRAD
  int copyLDID
  int scaleTicketID
  int logID
  int deliveryNumber
  String species
  double sumAmount
  int numberOfFields
  double totalAmount
  int countDecks
  int countLogs
  int countLogIDs
  Integer[] arrTDCK = new Integer [1000]
  Integer[] arrLGID = new Integer [1000]


  // Constructor 
  public LstLogByDeck(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.logger = logger
     this.utility = utility
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }
    
     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0      
     }

     totalAmount = 0d
     countDecks = 0
     countLogs = 0

     readEXTDST(inCONO, inDIVI, inDLNO)   

     if (scaleTicketID > 0) {
       
        //Get all decks for the delivery
        List<DBContainer> resultEXTSLH = listEXTSLH(inCONO, inDIVI, scaleTicketID) 
        for (DBContainer recLineEXTSLH : resultEXTSLH){ 
           copyTDCK = recLineEXTSLH.get("EXTDCK")
           fillArrTDCK(copyTDCK)
        }
       
       
        //Sum per deck/delivery
        countDecks = countDecks + 1
        for (int i = 0; i <= countDecks; i++ ) {
			    if (arrTDCK[i] != 0 && arrTDCK[i] != null)  {
				       //Sum the amounts and print line
				       countLogIDs = 0
               countLogs = 0
               totalAmount = 0
               copyCONO = 0
               copyDIVI = ""
               copyTDCK = 0
               copyLAMT = 0d
               copySPEC = ""
               copyLGID = 0
               
               length = 0d
               grossVolume = 0d
               netVolume = 0d
               sumLength = 0d
               sumGrossVolume = 0d
               sumNetVolume = 0d
               
 
  				     //List all log headers for the deck/delivery
               List<DBContainer> resultEXTSLHdeck = listEXTSLHdeck(inCONO, inDIVI, scaleTicketID, arrTDCK[i]) 
               TreeMap highValueSPEC= new TreeMap();
               for (DBContainer recLineEXTSLHdeck : resultEXTSLHdeck){ 
                 copyCONO = recLineEXTSLHdeck.get("EXCONO")
                 copyDIVI = recLineEXTSLHdeck.get("EXDIVI")
                 copyTDCK = recLineEXTSLHdeck.get("EXTDCK")
                 copyLAMT = recLineEXTSLHdeck.get("EXLAMT")
                 copySPEC = recLineEXTSLHdeck.get("EXSPEC")
                 copyLGID = recLineEXTSLHdeck.get("EXLGID")
                 
                 countLogs = countLogs+1
  
                 //List all log lines
                 //List and sum all records by deck/logID
    	           List<DBContainer> resultEXTSLD = listEXTSLD(inCONO, inDIVI, scaleTicketID, copyLGID) 
                 for (DBContainer recLineEXTSLD : resultEXTSLD){ 
                    length = recLineEXTSLD.get("EXLLEN")
                    grossVolume = recLineEXTSLD.get("EXLGRV")
                    netVolume = recLineEXTSLD.get("EXLNEV")
    
                    sumLength = sumLength + length
                    sumGrossVolume = sumGrossVolume + grossVolume
                    sumNetVolume = sumNetVolume + netVolume

                   
                     // Key might be present...
                     if (highValueSPEC.containsKey(copySPEC)) {
                        Integer value = highValueSPEC.get(copySPEC);
                        if (value != null) {
                           highValueSPEC.put(copySPEC,value+netVolume);
                        } else {
                           highValueSPEC.put(copySPEC,netVolume);
                        }
                     // Okay, there's a key but the value is null
                     } else {
                     // Definitely no such key
                     highValueSPEC.put(copySPEC,netVolume);
                     }                  
                 }
  
                 totalAmount = totalAmount + copyLAMT
               } //end list
               Map.Entry<String, Integer> entry = highValueSPEC.firstEntry();
               // Get the key and value of the entry
               String largestKey = entry.getKey();
               mi.outData.put("CONO", String.valueOf(copyCONO))
               mi.outData.put("DIVI", copyDIVI.toString())
               mi.outData.put("TDCK", String.valueOf(arrTDCK[i]))
               mi.outData.put("SPEC", largestKey)
               mi.outData.put("LLEN", String.valueOf(sumLength))
               mi.outData.put("LGRV", String.valueOf(sumGrossVolume))
               mi.outData.put("LNEV", String.valueOf(sumNetVolume))
               mi.outData.put("LAMT", String.valueOf(totalAmount))
               mi.outData.put("LLOG", String.valueOf(countLogs))
               mi.write() 
			  } //end if 
      } //end for loop

   } //end if
   
 } //end main
 
 
 

  //******************************************************************** 
  // Get STID from EXTDST 
  //******************************************************************** 
   private void readEXTDST(int CONO, String DIVI, int DLNO) {
      DBAction query = database.table("EXTDST").index("00").selection("EXSTID").build()
      DBContainer container = query.getContainer()
      container.set("EXCONO", CONO)
      container.set("EXDIVI", DIVI)
      container.set("EXDLNO", DLNO)
	  
	    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
      query.readAll(container, 3, pageSize, releasedItemProcessor)
   }
    
   Closure<?> releasedItemProcessor = { DBContainer container ->
      scaleTicketID = container.get("EXSTID")
   }



  //******************************************************************** 
  // Read records from scale ticket line table EXTSLH
  //********************************************************************  
  private List<DBContainer> listEXTSLH(int CONO, String DIVI, int STID){
    List<DBContainer> recLineEXTSLH = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTSLH")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTSLH").index("30").matching(expression).selectAllFields().build()
    DBContainer EXTSLH = query.createContainer()
    EXTSLH.set("EXCONO", CONO)
    EXTSLH.set("EXDIVI", DIVI)
    EXTSLH.set("EXSTID", STID)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTSLH, 3, pageSize, { DBContainer recordEXTSLH ->  
       recLineEXTSLH.add(recordEXTSLH.createCopy()) 
    })

    return recLineEXTSLH
  }


  //******************************************************************** 
  // Read records from scale ticket line table EXTSLH per deck
  //********************************************************************  
  private List<DBContainer> listEXTSLHdeck(int CONO, String DIVI, int STID, int TDCK){
    List<DBContainer>recLineEXTSLHdeck = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTSLH")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTSLH").index("50").matching(expression).selectAllFields().build()
    DBContainer EXTSLH = query.createContainer()
    EXTSLH.set("EXCONO", CONO)
    EXTSLH.set("EXDIVI", DIVI)
    EXTSLH.set("EXSTID", STID)
    EXTSLH.set("EXTDCK", TDCK)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTSLH, 4, pageSize, { DBContainer recordEXTSLHdeck ->  
       recLineEXTSLHdeck.add(recordEXTSLHdeck.createCopy()) 
    })

    return recLineEXTSLHdeck
  }



  //******************************************************************** 
	// fillArrTDCK - check if given TDCK is already in arrTDCK
  //******************************************************************** 
	public void fillArrTDCK(int TDCK) {
		//check arrTDCK is already including TDCK
		for (int i = 0; i < 1000; i++ ) {
			if (arrTDCK[i] == TDCK)  {
				break;
			}
			if (arrTDCK[i] == " " || arrTDCK[i] == null) {
				arrTDCK[i] = TDCK;
				countDecks++
				break;
			}
		}
	}


  //******************************************************************** 
	// fillArrLGID - check if given LGID is already in arrLGID
  //******************************************************************** 
	public void fillArrLGID(int LGID) {
		//check arrLGID is already including LGID
		for (int j = 0; j < 1000; j++ ) {
			if (arrLGID[j] == LGID)  {
				break;
			}
			if (arrLGID[j] == " " || arrLGID[j] == null) {
				arrLGID[j] = LGID;
				countLogIDs++
				break;
			}
		}
	}


  //******************************************************************** 
	// clearArrLGID - clear the array
  //******************************************************************** 
	public void clearArrLGID() {
		for (int i = 0; i < 1000; i++ ) {
			arrLGID[i] == null
		}
	}


  //******************************************************************** 
  // Read records from log details table EXTSLD
  //********************************************************************  
  private List<DBContainer> listEXTSLD(int CONO, String DIVI, int STID, int LGID){
    List<DBContainer>recLineEXTSLD = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTSLD")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTSLD").index("10").matching(expression).selectAllFields().build()
    DBContainer EXTSLD = query.createContainer()
    EXTSLD.set("EXCONO", CONO)
    EXTSLD.set("EXDIVI", DIVI)
    EXTSLD.set("EXSTID", STID)
    EXTSLD.set("EXLGID", LGID)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTSLD, 4, pageSize, { DBContainer recordEXTSLD ->  
       recLineEXTSLD.add(recordEXTSLD.createCopy()) 
    })

    return recLineEXTSLD
 }

 
}