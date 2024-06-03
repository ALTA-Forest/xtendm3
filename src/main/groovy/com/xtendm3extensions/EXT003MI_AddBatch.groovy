// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to add a batch to EXTDBH, EXTDBD, EXTDBI and EXTDBC
// Transaction AddBatch
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DBBU - Business Unit
 * @param: DBTP - Batch Type
 * @param: BDDT - Delivery Date Through
 * @param: USER - User
*/

/**
 * OUT
 * @return: CONO - Company Number
 * @return: DIVI - Division
 * @return: DBNO - Batch Number 
 * 
**/

import java.math.RoundingMode 
import java.math.BigDecimal
import java.lang.Math
import java.util.ArrayList


public class AddBatch extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inDBNO
  int outDBNO
  String nextNumber
  int deliveryNumber
  int deliveryDate
  String supplier
  int deliveryNumberBySUNO
  String supplierBySUNO
  double orderQty
  double shareAmount
  double sumOrderQty
  double sumShareAmount
  double orderQtyCharges
  double shareAmountCharges
  double sumOrderQtyCharges
  double sumShareAmountCharges
  String itemNumberCharges
  String newPayeeCharges
  String supplierCharges
  double chargeAmount
  double sumChargeAmount
  String itemNumber
  String costElement
  String facilityName
  String supplierName
  String itemCharges
  int deliveryCharges
  String payeeCharges
  int scaleTicketID
  int scaleTicketIDBySUNO
  String scaleTicketNumber
  int costSequence
  String costSequenceString
  int numberOfDeliveries
  int payerCONO
  String payerDIVI 
  int payerDLNO
  int payerSEQN
  int payerSTID
  String payerCASN 
  String payerITNO
  double payerCAAM
  String payerSUCM
  String payerSUNM
  int payerCF15
  int payerINBN
  String payerSUNO 
  int payerDBNO
  String paymentSUNO
  String paymentITNO
  double paymentPIAM
  double sumPaymentPIAM
  double sumTotalPaymentPIAM
  double sumOrderQtyChargesNewAmount
  double sumShareAmountChargesNewAmount
  String newSupplierCharges
  String newItemCharges
  String purchaseOrder
  int logID
  double payeeAmount
  ArrayList<String> arrCASN = new ArrayList<String>()
  ArrayList<String> arrSUNO = new ArrayList<String>()

  
  // Constructor 
  public AddBatch(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
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

     // Business Unit
     String inDBBU  
     if (mi.in.get("DBBU") != null && mi.in.get("DBBU") != "") {
        inDBBU = mi.inData.get("DBBU").trim() 
     } else {
        inDBBU = ""        
     }
           
     // Batch Type
     int inDBTP 
     if (mi.in.get("DBTP") != null) {
        inDBTP = mi.in.get("DBTP") 
     } else {
        inDBTP = 0        
     }
 
      // Delivery Date Through
     int inBDDT 
     if (mi.in.get("BDDT") != null) {
        inBDDT = mi.in.get("BDDT") 
		
		    //Validate date format
        boolean validBDDT = utility.call("DateUtil", "isDateValid", String.valueOf(inBDDT), "yyyyMMdd")  
        if (!validBDDT) {
           mi.error("Delivery Date Through is not valid")   
           return  
        } 

     } else {
        inBDDT = 0        
     }
  
     // User
     String inUSER  
     if (mi.in.get("USER") != null && mi.in.get("USER") != "") {
        inUSER = mi.inData.get("USER").trim() 
                
        // Validate buyer if entered
        Optional<DBContainer> CMNUSR = findCMNUSR(inCONO, inUSER)
        if (!CMNUSR.isPresent()) {
           mi.error("Buyer doesn't exist")   
           return             
        }

     } else {
        inUSER = ""        
     }

     //If Batch Type is 1
     if (inDBTP == 1) {
       
       //Get next number for batch numbers
       getNextNumber("", "L3", "1") 
       outDBNO = nextNumber as Integer
       inDBNO = outDBNO as Integer
  
       numberOfDeliveries = 0
       orderQty = 0d
       shareAmount = 0d
       sumOrderQty = 0d
       sumShareAmount = 0d
              
  
       //List all deliveries with status 30 and DLTP = inDBTP and FACI = inDBBU and DLDT <= inBDDT
       List<DBContainer> resultEXTDLH = listEXTDLH(inCONO, inDIVI, 30, inDBTP, inDBBU, inBDDT) 
       for (DBContainer recLineEXTDLH : resultEXTDLH) { 
              deliveryNumber = recLineEXTDLH.get("EXDLNO")
              deliveryDate = recLineEXTDLH.get("EXDLDT")
              supplier = recLineEXTDLH.get("EXSUNO")
              
              logger.debug("List from EXTDLH type ${inDBTP}")
              logger.debug("List from EXTDLH deliveryNumber ${deliveryNumber}")
              logger.debug("List from EXTDLH deliveryDate ${deliveryDate}")
              logger.debug("List from EXTDLH supplier ${supplier}")

              //Set delivery header status to 40
              updEXTDLHRecord(inCONO, inDIVI, deliveryNumber) 
              
              //Count deliveries
              numberOfDeliveries = numberOfDeliveries + 1

              // Get Scale Ticket info
              Optional<DBContainer> EXTDST = findEXTDST(inCONO, inDIVI, deliveryNumber)
              if(EXTDST.isPresent()){
                 DBContainer containerEXTDST = EXTDST.get() 
                 scaleTicketID = containerEXTDST.get("EXSTID") 

                 //Update EXTDPS with Batch Number
                 updEXTDPSRecord(inCONO, inDIVI, scaleTicketID)

                 //List all scale ticket lines
                 List<DBContainer> resultEXTDSL = listEXTDSL(inCONO, inDIVI, scaleTicketID) 
                 for (DBContainer recLineEXTDSL : resultEXTDSL){ 
                    itemNumber = recLineEXTDSL.get("EXITNO") 
                    orderQty = recLineEXTDSL.get("EXORQT")
                    shareAmount = recLineEXTDSL.get("EXSTAM")
                    sumOrderQty = sumOrderQty + orderQty
                    sumShareAmount = sumShareAmount + shareAmount

                    addEXTDBDRecord(inCONO, inDIVI, inDBNO, deliveryNumber, supplier, itemNumber, scaleTicketID, orderQty, shareAmount)
                 }
                 
                 
                 
                 //List all payers 
                 List<DBContainer> resultEXTDPSpayers = listEXTDPS(inCONO, inDIVI, scaleTicketID) 
                 for (DBContainer recLineEXTDPS : resultEXTDPSpayers){ 
                   payerCASN = recLineEXTDPS.get("EXCASN") 
                   fillArrCASN(payerCASN.trim())
                 }
                 
                 
                 //List all payer lines to prep for summery
                 List<DBContainer> resultEXTDPS = listEXTDPS(inCONO, inDIVI, scaleTicketID) 
                 for (DBContainer recLineEXTDPS : resultEXTDPS){ 
                    payerCONO = inCONO 
                    payerDIVI = inDIVI
                    payerDLNO = recLineEXTDPS.get("EXDLNO") 
                    payerSEQN = recLineEXTDPS.get("EXSEQN") 
                    payerSTID = recLineEXTDPS.get("EXSTID") 
                    payerCASN = recLineEXTDPS.get("EXCASN") 
                    payerITNO = recLineEXTDPS.get("EXITNO") 
                    payerCAAM = recLineEXTDPS.get("EXCAAM")
                    payerSUCM = recLineEXTDPS.get("EXSUCM")
                    payerSUNM = recLineEXTDPS.get("EXSUNM") 
                    payerCF15 = recLineEXTDPS.get("EXCF15") 
                    payerINBN = recLineEXTDPS.get("EXINBN") 
                    payerSUNO = supplier
                    payerDBNO = inDBNO

                    orderQty = 0d
                    purchaseOrder = ""

                    //Get ORQT from EXTDPD
                    Optional<DBContainer> EXTDPD = findEXTDBD(inCONO, inDIVI, payerDBNO, payerDLNO, payerSUNO, payerITNO)
                    if(EXTDPD.isPresent()){
                       DBContainer containerEXTDPD = EXTDPD.get() 
                       orderQty = containerEXTDPD.get("EXORQT") 
                    }
                    double price = 0d
                    if (payerCAAM != 0d && orderQty != 0d) {
                       price = payerCAAM/orderQty
                       BigDecimal priceRounded  = BigDecimal.valueOf(price) 
                       priceRounded = priceRounded.setScale(6, RoundingMode.HALF_UP) 
                       price = priceRounded 
                    }
                    addEXTDBCRecord(payerCONO, payerDIVI, inDBNO, String.valueOf(payerDLNO), payerITNO, payerCASN, payerSUNM, String.valueOf(payerSEQN), payerSUCM, 0, 0, orderQty, payerCAAM, price) 
                 }

              }

       }

       for (int i = 0; i <= arrCASN.size()-1; i++ ) {
			     lstEXTDPSforEXTDBIbyCASN(inCONO, inDIVI, inDBNO, arrCASN[i])
			     addEXTDBIRecord(inCONO, inDIVI, inDBNO, arrCASN[i], sumChargeAmount)
			 }


       // Get Facility name
       Optional<DBContainer> CFACIL = findCFACIL(inCONO, inDBBU)
       if(CFACIL.isPresent()){
          DBContainer containerCFACIL = CFACIL.get() 
          facilityName = containerCFACIL.get("CFFACN") 
       }
       
       addEXTDBHRecord(inCONO, inDIVI, inDBNO, inDBTP, inDBBU, facilityName, numberOfDeliveries, sumShareAmount, inBDDT, inUSER)
  

       //**********************************************************************************************

       mi.outData.put("CONO", String.valueOf(inCONO)) 
       mi.outData.put("DIVI", inDIVI) 
       mi.outData.put("DBNO", String.valueOf(inDBNO)) 
       mi.write()
      
  } else if (inDBTP == 2) {
       
       //Get next number for batch numbers
       getNextNumber("", "L3", "1") 
       outDBNO = nextNumber as Integer
       inDBNO = outDBNO as Integer
  
       numberOfDeliveries = 0
       paymentPIAM = 0d
       sumPaymentPIAM = 0d
       paymentSUNO = ""
       paymentITNO = ""

       //List all deliveries with status 30 and DLTP = inDBTP and FACI = inDBBU and DLDT <= inBDDT
       List<DBContainer> resultEXTDLH = listEXTDLH(inCONO, inDIVI, 30, inDBTP, inDBBU, inBDDT) 
       for (DBContainer recLineEXTDLH : resultEXTDLH){ 
              deliveryNumber = recLineEXTDLH.get("EXDLNO")
              deliveryDate = recLineEXTDLH.get("EXDLDT")
              supplier = recLineEXTDLH.get("EXSUNO")

              logger.debug("List from EXTDLH type ${inDBTP}")
              logger.debug("List from EXTDLH deliveryNumber ${deliveryNumber}")
              logger.debug("List from EXTDLH deliveryDate ${deliveryDate}")
              logger.debug("List from EXTDLH supplier ${supplier}")


              //Set delivery header status to 40
              updEXTDLHRecord(inCONO, inDIVI, deliveryNumber) 
              
              //Count deliveries
              numberOfDeliveries = numberOfDeliveries + 1
              
              sumPaymentPIAM = 0d
              
              //Update EXTCPI with Batch Number
              updEXTCPIRecord(inCONO, inDIVI, deliveryNumber)

              // Get Contract Payment info
              List<DBContainer> resultEXTCPI = listEXTCPI(inCONO, inDIVI, deliveryNumber) 
              for (DBContainer recLineEXTCPI : resultEXTCPI){ 
                 paymentSUNO = recLineEXTCPI.get("EXSUNO") 
                 paymentITNO = recLineEXTCPI.get("EXITNO")
                 paymentPIAM = recLineEXTCPI.get("EXPIAM")

                 fillArrSUNO(paymentSUNO.trim())

                 logger.debug("List from EXTCPI per delivery DLNO ${deliveryNumber}")
                 logger.debug("List from EXTCPI SUNO ${paymentSUNO}")
                 logger.debug("List from EXTCPI PIAM ${paymentPIAM}")

                 addEXTDBDRecord(inCONO, inDIVI, inDBNO, deliveryNumber, paymentSUNO, paymentITNO, 0, 0, paymentPIAM)
              }
              
              

       }
       
       sumTotalPaymentPIAM = 0d
       
       for (int i = 0; i <= arrSUNO.size()-1; i++ ) {
           sumPaymentPIAM = 0d
			     
			     lstEXTCPIforEXTDBIbySUNO(inCONO, inDIVI, inDBNO, arrSUNO[i])
			     
			     logger.debug("List from EXTCPI SUNO ${arrSUNO[i]}")
				   logger.debug("List from EXTCPI DBNO ${inDBNO}")
				   logger.debug("List from EXTCPI sumPaymentPIAM ${sumPaymentPIAM}")
           
				   sumTotalPaymentPIAM = sumTotalPaymentPIAM + sumPaymentPIAM

			     addEXTDBIRecord(inCONO, inDIVI, inDBNO, arrSUNO[i], sumPaymentPIAM)
			 }

       // Get Facility name
       Optional<DBContainer> CFACIL = findCFACIL(inCONO, inDBBU)
       if(CFACIL.isPresent()){
          DBContainer containerCFACIL = CFACIL.get() 
          facilityName = containerCFACIL.get("CFFACN") 
       }
       
       addEXTDBHRecord(inCONO, inDIVI, inDBNO, inDBTP, inDBBU, facilityName, numberOfDeliveries, sumTotalPaymentPIAM, inBDDT, inUSER)
  
       //****************************************************************************************
       mi.outData.put("CONO", String.valueOf(inCONO)) 
       mi.outData.put("DIVI", inDIVI) 
       mi.outData.put("DBNO", String.valueOf(inDBNO)) 
       mi.write()
    
  }//End if inDBTP
  
  
} //End main


   //***************************************************************************** 
   // Get next number in the number serie using CRS165MI.RtvNextNumber    
   // Input 
   // Division
   // Number Series Type
   // Number Seriew
   //***************************************************************************** 
   void getNextNumber(String division, String numberSeriesType, String numberSeries){   
        Map<String, String> params = [DIVI: division, NBTY: numberSeriesType, NBID: numberSeries] 
        Closure<?> callback = {
        Map<String, String> response ->
          if(response.NBNR != null){
            nextNumber = response.NBNR
          }
        }

        miCaller.call("CRS165MI","RtvNextNumber", params, callback)
   } 
  
  
  //******************************************************************** 
  // Get EXTDST record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDST(int CONO, String DIVI, int DLNO){  
     DBAction query = database.table("EXTDST").index("00").selection("EXSTID").build()
     DBContainer EXTDST = query.getContainer()
     EXTDST.set("EXCONO", CONO)
     EXTDST.set("EXDIVI", DIVI)
     EXTDST.set("EXDLNO", DLNO)
     if(query.read(EXTDST))  { 
       return Optional.of(EXTDST)
     } 
  
     return Optional.empty()
  }


	
	//**************************************************************
	// fillArrCASN - check if given CASN is already in arrCASN
	//**************************************************************
	public void fillArrCASN(String CASN) {
		if (arrCASN.contains(CASN)) {
		} else {
		  arrCASN.add(CASN)
		}
	}
	
	
	
	//**************************************************************
	// fillArrSUNO - check if given SUNO is already in arrSUNO
	//**************************************************************
	public void fillArrSUNO(String SUNO) {
		if (arrSUNO.contains(SUNO)) {
		} else {
		  arrSUNO.add(SUNO)
		}
	}
	

	
  //******************************************************************** 
  // Get CFACIL record
  //******************************************************************** 
  private Optional<DBContainer> findCFACIL(int CONO, String FACI){  
     DBAction query = database.table("CFACIL").index("00").selection("CFFACN").build()
     DBContainer CFACIL = query.getContainer()
     CFACIL.set("CFCONO", CONO)
     CFACIL.set("CFFACI", FACI)
     if(query.read(CFACIL))  { 
       return Optional.of(CFACIL)
     } 
  
     return Optional.empty()
  }


   //******************************************************************** 
   // Check User
   //******************************************************************** 
   private Optional<DBContainer> findCMNUSR(int CONO, String USID){  
     DBAction query = database.table("CMNUSR").index("00").build()   
     DBContainer CMNUSR = query.getContainer()
     CMNUSR.set("JUCONO", 0)
     CMNUSR.set("JUDIVI", "")
     CMNUSR.set("JUUSID", USID)
    
     if(query.read(CMNUSR))  { 
       return Optional.of(CMNUSR)
     } 
  
     return Optional.empty()
   }


  //******************************************************************** 
  // Get EXTDBD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDBD(int CONO, String DIVI, int DBNO, int DLNO, String SUNO, String ITNO){  
     DBAction query = database.table("EXTDBD").index("00").selection("EXORQT").build()
     DBContainer EXTDBD = query.getContainer()
     EXTDBD.set("EXCONO", CONO)
     EXTDBD.set("EXDIVI", DIVI)
     EXTDBD.set("EXDBNO", DBNO)
     EXTDBD.set("EXDLNO", DLNO)
     EXTDBD.set("EXSUNO", SUNO)
     EXTDBD.set("EXITNO", ITNO)
     if(query.read(EXTDBD))  { 
       return Optional.of(EXTDBD)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Read records from delivery header table EXTDLH
  //********************************************************************  
  private List<DBContainer> listEXTDLH(int CONO, String DIVI, int STAT, int DLTP, String FACI, int DLDT){
    List<DBContainer>recLineEXTDLH = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTDLH")
    expression = expression.eq("EXCONO", String.valueOf(CONO)).and(expression.eq("EXDIVI", DIVI)).and(expression.eq("EXSTAT", String.valueOf(STAT))).and(expression.eq("EXDLTP", String.valueOf(DLTP))).and(expression.eq("EXFACI", FACI)).and(expression.le("EXDLDT", String.valueOf(DLDT)))

    DBAction query = database.table("EXTDLH").index("10").matching(expression).selectAllFields().build()
    DBContainer EXTDLH = query.createContainer()
    EXTDLH.set("EXCONO", CONO)
    EXTDLH.set("EXDIVI", DIVI)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords() 
    query.readAll(EXTDLH, 2, pageSize, { DBContainer recordEXTDLH ->  
       recLineEXTDLH.add(recordEXTDLH.createCopy()) 
    })

    return recLineEXTDLH
  }


  //******************************************************************** 
  // Read records from scale ticket line table EXTDSL
  //********************************************************************  
  private List<DBContainer> listEXTDSL(int CONO, String DIVI, int STID){
    List<DBContainer>recLineEXTDSL = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTDSL")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTDSL").index("00").matching(expression).selectAllFields().build()
    DBContainer EXTDSL = query.createContainer()
    EXTDSL.set("EXCONO", CONO)
    EXTDSL.set("EXDIVI", DIVI)
    EXTDSL.set("EXSTID", STID)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords() 
    query.readAll(EXTDSL, 3, pageSize, { DBContainer recordEXTDSL ->  
       recLineEXTDSL.add(recordEXTDSL.createCopy()) 
    })

    return recLineEXTDSL
  }


  //******************************************************************** 
  // Read records from contract payment table EXTCPI
  //********************************************************************  
  private List<DBContainer> listEXTCPI(int CONO, String DIVI, int DLNO){
    List<DBContainer>recLineEXTCPI = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTCPI")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTCPI").index("10").matching(expression).selectAllFields().build()
    DBContainer EXTCPI = query.createContainer()
    EXTCPI.set("EXCONO", CONO)
    EXTCPI.set("EXDIVI", DIVI)
    EXTCPI.set("EXDLNO", DLNO)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords() 
    query.readAll(EXTCPI, 3, pageSize, { DBContainer recordEXTCPI ->  
       recLineEXTCPI.add(recordEXTCPI.createCopy()) 
    })

    return recLineEXTCPI
  }


  //******************************************************************** 
  // Read records from payee split table EXTDPS
  //********************************************************************  
  private List<DBContainer> listEXTDPS(int CONO, String DIVI, int STID){
    List<DBContainer>recLineEXTDPS = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTDPS")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTDPS").index("20").matching(expression).selectAllFields().build()
    DBContainer EXTDPS = query.createContainer()
    EXTDPS.set("EXCONO", CONO)
    EXTDPS.set("EXDIVI", DIVI)
    EXTDPS.set("EXSTID", STID)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords() 
    query.readAll(EXTDPS, 3, pageSize, { DBContainer recordEXTDPS ->  
       recLineEXTDPS.add(recordEXTDPS.createCopy()) 
    })

    return recLineEXTDPS
  }
  



  //******************************************************************** 
  // Add EXTDBH record 
  //********************************************************************     
  void addEXTDBHRecord(int CONO, String DIVI, int DBNO, int DBTP, String DBBU, String BUNA, int BDEL, double BTOT, int BDDT, String USER) {  
       DBAction action = database.table("EXTDBH").index("00").build()
       DBContainer EXTDBH = action.createContainer()
       EXTDBH.set("EXCONO", CONO)
       EXTDBH.set("EXDIVI", DIVI)
       EXTDBH.set("EXDBNO", DBNO)
       EXTDBH.set("EXDBTP", DBTP)
       EXTDBH.set("EXDBBU", DBBU)
       EXTDBH.set("EXBUNA", BUNA)
       EXTDBH.set("EXBDEL", BDEL)
       EXTDBH.set("EXBTOT", BTOT)
       EXTDBH.set("EXBDDT", BDDT)
       EXTDBH.set("EXUSER", USER)
       EXTDBH.set("EXSTAT", 10)
       EXTDBH.set("EXCHID", program.getUser())
       EXTDBH.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTDBH.set("EXRGDT", regdate) 
       EXTDBH.set("EXLMDT", regdate) 
       EXTDBH.set("EXRGTM", regtime)
       action.insert(EXTDBH)         
 } 



  //******************************************************************** 
  // Calc charges per payee to add to EXTDBI
  //********************************************************************  
  void lstEXTDPSforEXTDBIbyCASN(int CONO, String DIVI, int DBNO, String CASN) {   
     
     chargeAmount = 0d
     sumChargeAmount = 0d

     ExpressionFactory expression = database.getExpressionFactory("EXTDPS")
   
     expression = expression.eq("EXCONO", String.valueOf(CONO)).and(expression.eq("EXDIVI", DIVI)).and(expression.eq("EXINBN", String.valueOf(DBNO))).and(expression.eq("EXCASN", CASN))

     // List charges by payee  
     DBAction actionline = database.table("EXTDPS").index("40").matching(expression).selection("EXDLNO", "EXCAAM").build()   
     DBContainer line = actionline.getContainer()  
     
     // Read with one key  
     line.set("EXCONO", CONO)  
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()   
     actionline.readAll(line, 1, pageSize, releasedLineProcessorEXTDPS)   
   
   }

  //******************************************************************** 
  // List charges
  //********************************************************************  
  Closure<?> releasedLineProcessorEXTDPS = { DBContainer line ->   
         int dlno = line.get("EXDLNO")

         chargeAmount = line.get("EXCAAM")
         sumChargeAmount = sumChargeAmount + chargeAmount
  }



  //******************************************************************** 
  // Calc charges per payee to add to EXTDBC
  //********************************************************************  
  void lstEXTDPSforEXTDBCbyCASN(int CONO, String DIVI, int DBNO, String CASN) {   
     
     ExpressionFactory expression = database.getExpressionFactory("EXTDPS")
   
     expression = expression.eq("EXCONO", String.valueOf(CONO)).and(expression.eq("EXDIVI", DIVI)).and(expression.eq("EXINBN", String.valueOf(DBNO))).and(expression.eq("EXCASN", CASN))

     // List charges by payee  
     DBAction actionline = database.table("EXTDPS").index("40").matching(expression).selectAllFields().build()   
     DBContainer line = actionline.getContainer()  
     
     // Read with one key  
     line.set("EXCONO", CONO)  
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()   
     actionline.readAll(line, 1, pageSize, releasedLineProcessorEXTDPSbyCASN)   
   
   }

  //******************************************************************** 
  // List charges
  //********************************************************************  
  Closure<?> releasedLineProcessorEXTDPSbyCASN = { DBContainer line ->   
         payerDLNO = line.get("EXDLNO")
         payerCONO = line.get("EXCONO")
         payerDIVI = line.get("EXDIVI")
         payerITNO = line.get("EXITNO")
         payerCASN = line.get("EXCASN")
         payerSUNM = line.get("EXSUNM")
         payerSEQN = line.get("EXSEQN")
         payerSUCM = line.get("EXCONO")
         payerCAAM = line.get("EXCAAM")
         
         orderQty = 0d
         purchaseOrder = ""

         //Get ORQT from EXTDPD
         Optional<DBContainer> EXTDPD = findEXTDBD(inCONO, inDIVI, inDBNO, payerDLNO, supplier, payerITNO)
         if(EXTDPD.isPresent()){
            DBContainer containerEXTDPD = EXTDPD.get() 
            orderQty = containerEXTDPD.get("EXORQT") 
         }
         double price = payerCAAM/orderQty
         BigDecimal priceRounded  = BigDecimal.valueOf(price) 
         priceRounded = priceRounded.setScale(6, RoundingMode.HALF_UP) 
         price = priceRounded 

         addEXTDBCRecord(payerCONO, payerDIVI, inDBNO, String.valueOf(payerDLNO), payerITNO, payerCASN, payerSUNM, String.valueOf(payerSEQN), payerSUCM, 0, 0, orderQty, payerCAAM, price) 
  }



  //******************************************************************** 
  // Calc charges per supplier to add to EXTDBI
  //********************************************************************  
  void lstEXTCPIforEXTDBIbySUNO(int CONO, String DIVI, int DBNO, String SUNO) {   
     
     chargeAmount = 0d
     sumChargeAmount = 0d

     ExpressionFactory expression = database.getExpressionFactory("EXTCPI")
   
     expression = expression.eq("EXCONO", String.valueOf(CONO)).and(expression.eq("EXDIVI", DIVI)).and(expression.eq("EXINBN", String.valueOf(DBNO))).and(expression.eq("EXSUNO", SUNO))

     // List charges by payee  
     DBAction actionline = database.table("EXTCPI").index("30").matching(expression).selection("EXPIAM").build()   
     DBContainer line = actionline.getContainer()  
     
     // Read with one key  
     line.set("EXCONO", CONO)  
     
     int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()   
     actionline.readAll(line, 1, pageSize, releasedLineProcessorEXTCPI)   
   
   }

  //******************************************************************** 
  // List charges
  //********************************************************************  
  Closure<?> releasedLineProcessorEXTCPI = { DBContainer line ->   

         paymentPIAM = line.get("EXPIAM")
         sumPaymentPIAM = sumPaymentPIAM + paymentPIAM
  }


  //******************************************************************** 
  // Add EXTDBD record 
  //********************************************************************     
  void addEXTDBDRecord(int CONO, String DIVI, int DBNO, int DLNO, String SUNO, String ITNO, int STID, double ORQT, double STAM) {  
       DBAction action = database.table("EXTDBD").index("00").build()
       DBContainer EXTDBD = action.createContainer()
       EXTDBD.set("EXCONO", CONO)
       EXTDBD.set("EXDIVI", DIVI)
       EXTDBD.set("EXDBNO", DBNO)
       EXTDBD.set("EXDLNO", DLNO)
       EXTDBD.set("EXSUNO", SUNO)
       EXTDBD.set("EXITNO", ITNO)
       EXTDBD.set("EXSTID", STID)
       EXTDBD.set("EXORQT", ORQT)
       EXTDBD.set("EXSTAM", STAM)
       EXTDBD.set("EXCHID", program.getUser())
       EXTDBD.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTDBD.set("EXRGDT", regdate) 
       EXTDBD.set("EXLMDT", regdate) 
       EXTDBD.set("EXRGTM", regtime)
       action.insert(EXTDBD)         
 } 



   //******************************************************************** 
  // Add EXTDBI record 
  //********************************************************************     
  void addEXTDBIRecord(int CONO, String DIVI, int DBNO, String SUNO, double BIAM) {  
       DBAction action = database.table("EXTDBI").index("00").build()
       DBContainer EXTDBI = action.createContainer()
       EXTDBI.set("EXCONO", CONO)
       EXTDBI.set("EXDIVI", DIVI)
       EXTDBI.set("EXDBNO", DBNO)
       EXTDBI.set("EXSUNO", SUNO)
       EXTDBI.set("EXBIAM", BIAM)
       EXTDBI.set("EXCHID", program.getUser())
       EXTDBI.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTDBI.set("EXRGDT", regdate) 
       EXTDBI.set("EXLMDT", regdate) 
       EXTDBI.set("EXRGTM", regtime)
       action.insert(EXTDBI)         
 } 


 
  //******************************************************************** 
  // Add EXTDBC record 
  //********************************************************************     
  void addEXTDBCRecord(int CONO, String DIVI, int DBNO, String TREF, String ITNO, String CASN, String SUNM, String CDSE, String SUCM, int INBN, int INDT, double INQT, double INAM, double GRPR) {  
       DBAction action = database.table("EXTDBC").index("00").build()
       DBContainer EXTDBC = action.createContainer()
       EXTDBC.set("EXCONO", CONO)
       EXTDBC.set("EXDIVI", DIVI)
       EXTDBC.set("EXDBNO", DBNO)
       EXTDBC.set("EXTREF", TREF)
       EXTDBC.set("EXITNO", ITNO)
       EXTDBC.set("EXCASN", CASN)
       EXTDBC.set("EXSUNM", SUNM)
       EXTDBC.set("EXCDSE", CDSE)
       EXTDBC.set("EXSUCM", SUCM)
       EXTDBC.set("EXINBN", INBN)
       EXTDBC.set("EXINDT", INDT)
       EXTDBC.set("EXINQT", INQT)
       EXTDBC.set("EXINAM", INAM)
       EXTDBC.set("EXGRPR", GRPR)
       EXTDBC.set("EXCHID", program.getUser())
       EXTDBC.set("EXCHNO", 1) 
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTDBC.set("EXRGDT", regdate) 
       EXTDBC.set("EXLMDT", regdate) 
       EXTDBC.set("EXRGTM", regtime)
       action.insert(EXTDBC)         
 } 



  //******************************************************************** 
  // Update EXTDLH record
  //********************************************************************    
  void updEXTDLHRecord(int CONO, String DIVI, int DLNO) {      
     DBAction action = database.table("EXTDLH").index("00").build()
     DBContainer EXTDLH = action.getContainer()
     EXTDLH.set("EXCONO", CONO)
     EXTDLH.set("EXDIVI", DIVI)
     EXTDLH.set("EXDLNO", DLNO)

     // Read with lock
     action.readLock(EXTDLH, updateCallBackEXTDLH)
     }
   
     Closure<?> updateCallBackEXTDLH = { LockedResult lockedResult -> 
       //Set status to 40
       lockedResult.set("EXSTAT", 40)
  
       // Update changed information
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeDate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeDate)  
        
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }




  //******************************************************************** 
  // Update EXTDPS record
  //********************************************************************    
  void updEXTDPSRecord(int CONO, String DIVI, int STID) {      
     DBAction action = database.table("EXTDPS").index("20").build()
     DBContainer EXTDPS = action.getContainer()
     EXTDPS.set("EXCONO", CONO)
     EXTDPS.set("EXDIVI", DIVI)
     EXTDPS.set("EXSTID", STID)

     // Read with lock
     action.readAllLock(EXTDPS, 3, updateCallBackEXTDPS)
     }
   
     Closure<?> updateCallBackEXTDPS = { LockedResult lockedResult -> 
       //Update with batch number
       lockedResult.set("EXINBN", inDBNO)
  
       // Update changed information
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeDate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeDate)  
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
  }


  //******************************************************************** 
  // Update EXTCPI record
  //********************************************************************    
  void updEXTCPIRecord(int CONO, String DIVI, int DLNO) {      
     DBAction action = database.table("EXTCPI").index("10").build()
     DBContainer EXTCPI = action.getContainer()
     EXTCPI.set("EXCONO", CONO)
     EXTCPI.set("EXDIVI", DIVI)
     EXTCPI.set("EXDLNO", DLNO)

     // Read with lock
     action.readAllLock(EXTCPI, 3, updateCallBackEXTCPI)
     }
   
     Closure<?> updateCallBackEXTCPI = { LockedResult lockedResult -> 
       //Update with batch number
       lockedResult.set("EXINBN", inDBNO)
  
       // Update changed information
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeDate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeDate)  
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
  }

} 

