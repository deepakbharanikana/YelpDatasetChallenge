/**
 * @author Milind Gokhale
 * This code contains utility functions which are used in the project in various other classes.
 * Date : December 3, 2015
 */

package com.search.project.yelp.task1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.HashMap;

import com.search.project.yelp.task1.datatypes.Review;

/**
 * @author Milind
 * 
 * @info Class to contain the utility functions
 *
 */
public class UtilFunctions {

	/**
	 * This function returns the path of the src directory in the project file
	 * heirarchy.
	 * 
	 * @return sourcePath
	 */
	public static String getMySourcePath() {
		URL location = Review.class.getProtectionDomain().getCodeSource()
				.getLocation();
		String srcPath = location.toString().replace("file:/", "")
				.replace("bin", "src");
		return srcPath;
	}

	/**
	 * This function returns the hashmap containing categories and their
	 * features/bag of words Hashmap structure: categoryFeaturesMap [Key:
	 * CategoryName; Value: bag of words for category]
	 * 
	 * The categoryFeaturesMap is serialized and written in a list.ser file.
	 * This functions reads the list.ser file and deseralizes to construct the
	 * map again.
	 * 
	 * @return categoryFeaturesMap
	 * 
	 */
	public static HashMap<String, String> getCategoryFeaturesMap() {
		FileInputStream fis;
		HashMap<String, String> categoryFeaturesMap = null;
		try {
			fis = new FileInputStream("C:/searchproject/list.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			categoryFeaturesMap = (HashMap<String, String>) ois.readObject();

			ois.close();
			fis.close();
			// System.out.println(categoryFeaturesMap);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// categoryFeaturesMap = new HashMap<String, String>();
		// categoryFeaturesMap
		// .put("Eyewear & Opticians",
		// "*dispensing**optician**correction**vision**opthalmic**eyesight**sunglasses**glasses**goggles**designer**diesel**adidas**frames**lens**sight**eyewear*");
		// categoryFeaturesMap
		// .put("Nutritionists",
		// "*nutrition**impacts**health**dietitian**nutritionists**vitamins**minerals**carbs*");
		// categoryFeaturesMap
		// .put("Orthopedists",
		// "*arthritis**osteoarthritis**fractures**joint**replacement**spine**hip**foot**ankle**bone**joint**tendon**nerve**shoulder**elbow**runners**knee**erbs**palsy**tendonitis**neurofibromatosis**rickets**lyme**sciatica**cauda**equina**scoliosis**spinal**tenosis**cubital**carpal*");
		// categoryFeaturesMap
		// .put("Turkish",
		// "*ottoman**kebab**simit**menemen**pilaf**baklava**kadayif**manti**karniyarik**hunkarbegendi**dolma**tekmil**lahana**baklali**enginar**turkish*");
		// categoryFeaturesMap
		// .put("Chinese",
		// "*dimsum**szechuan**cantonese**mandarin**anhui**fujian**hunan**jiangsu**shandong**zhejiang**springroll**momos**wontons**dumplings**chow**mein**peking**chopsuey**manchow**kung**chinese*");
		// categoryFeaturesMap
		// .put("Slovakian",
		// "*bryndza**kapustnica**dolky**bryndzove**halushky**gulash**slovakian*");
		// categoryFeaturesMap
		// .put("French",
		// "*bisque**french**croissant**eclair**macarons**madeleine**souffle**crepes**tarte**quiche**fondue**potaufeu**foie**gras**confit**bouillabaisse**ratatouille**creperies*");
		// categoryFeaturesMap
		// .put("Indonesian",
		// "*tumpeng**sambal**satay**bakso**soto**nasi**gudeg**baso**gado**ayam**goreng**cabai**indonesian*");
		// categoryFeaturesMap.put("Cambodian",
		// "*khmer**amok**prahok**kuyteav**sach**cambodian*");
		// categoryFeaturesMap
		// .put("American",
		// "*hotdog**pretzels**tex-mex**bagels**burgers**cookies**applepie**fries**corndog**cupcakes**salads**macroni**crabcakes**penutbutter**blt**pickles**cajun**creole**southern**american*");
		// categoryFeaturesMap
		// .put("Japanese",
		// "*sushi**donburi**onigiri**kare**raisu**chahan**chazuke**kayu**sashimi**yakizakana**soba**ramen**somen**yakisoba**nabe**oden**sukiyaki**shabu**chanko**yakitori**tonkatsu**yakiniku**nikujaga**teppanyaki**hiyayakko**yudofu**agedashidofu**yoshoku**korokke**miso**omuraisu**hayashi**hambagu**tempura**okonomiyaki**monjayaki**gyoza**chawanmushi**japanese*");
		// categoryFeaturesMap.put("Czech",
		// "*dumplings**goulash**schnitzel**koleno**czech*");
		// categoryFeaturesMap
		// .put("Iranian and Persian",
		// "*koloocheh**masgati**pashmak**baghlavaa**qottab**chelow**kabab**khoresh**kuku**polo**dates**doogh**naan**dolma**saffron**kebab**morgh**shirazi**shir**berenj**maast**desser**miveh**iranian**persian*");
		// categoryFeaturesMap
		// .put("Russian",
		// "*beef**stroganov**bliny**caviar**coulibiac**dressed**herring**kasha**kissel**knish**kalduny**kholodets**kulich**kvass**lymonnyk**medovukha**okroshka**shchi**shashlyk**pirozhki**russian*");
		// categoryFeaturesMap
		// .put("Canadian",
		// "*maple**syrup**poutine**butter**tarts**nanaimo**bar**bannock**tourtiere**ketchup**chips**bacon**beavertails**fiddleheads**swiss**chalet**sauce**montreal**smoked**meat**canadian*");
		// categoryFeaturesMap
		// .put("Mexican",
		// "*tacos**nachos**burrito**tortilla**enchilada**guacamole**fajitas**baja**black**beans**chimichangas**queso**blanco**dip**salsa**chili**peppers**jalepeno**corn**rice**quesadilla**casserole**chalupa**chorizo**taquitos**chimichangas**pico**gallo**churros**cheese**mexican*");
		// categoryFeaturesMap
		// .put("Indian",
		// "*samosas**chicken**tikka**punjabi**curry**naan**paneer**paratha**sambhar**dhokla**rice**chaat**pakora**masala**chutney**rajma**tandoor**jalebi**chai**jamun**indian*");
		// categoryFeaturesMap
		// .put("Mongolian",
		// "*aaruul**airag**khorhog**boodog**gulyash**khuushuur**buuz**bansh**lapsha**dumplings**tsuivan**guriltai**shul**bantan**boortsog**mongolian*");
		// categoryFeaturesMap.put("Ukrainian",
		// "*borshch**varenyky**banosh**brynza**uzvar**paska**ukrainian*");
		// categoryFeaturesMap
		// .put("Latin American",
		// "*cachapas**arepa**venezuelan**churrasco**empanada**tamal**chipa**feijoada**cod**fritters**pernil**alfajores**chimichurri**quinoa**pozole**cassava**gallo**pinto**latin**american**caribbean**brazilian**argentine**peruvian**columbian*");
		// categoryFeaturesMap
		// .put("Optometrists",
		// "*vision**health**problems**refractice**errors**therapy**pre**post**operative**routine**exams**refer**testing**glasses**tonometer**drops**dilation**examination**topical**therapeutic**measure**optics**retina**lens*");
		// categoryFeaturesMap
		// .put("Burmese",
		// "*tea**leaf**salad**shan**fish**mohinga**lahpet**kauk**swe**thoke**burmese*");
		// categoryFeaturesMap
		// .put("Polish",
		// "*pierogi**rosol**zurek**soup**barszcz**bigos**golabki**kopytka**goulash**polish*");
		// categoryFeaturesMap
		// .put("Middle Eastern",
		// "*hummus**manakeesh**halloumi**meddamas**falafel**tabouleh**ghanoush**fattoush**shanklish**shawarma**shish**tawook**dolma**baklava**knafeh**masgouf**gyro**pita**kibbeh**chickpea**garlic**mediterranean**arabian**middle**eastern**labanese*");
		// categoryFeaturesMap
		// .put("Malaysian",
		// "*roti**canai**jala**asam**laksa**curry**satay**nasi**lemak**popiah**asam**pedas**malaysian*");
		// categoryFeaturesMap
		// .put("Scottish",
		// "*porridge**haggis**tatties**neeps**scottish**shortbread**bridie**apple**bramble**pie**kippers**pudding**stovies**lorne**sausage**cranachan**atholl**brose**tablet**clootie**dumpling**scotch**scottish*");
		// categoryFeaturesMap
		// .put("Italian",
		// "*pizza**pasta**spheghetti**penne**macroni**cheese**lasagne**garlic**bread**bolognese**sauce**mozarella**fusilli**ravioli**tortellini**gnocchi**ricotta**pesto**risotto**tiramisu**minestrone**truffles**italian*");
		// categoryFeaturesMap
		// .put("Portugese",
		// "*frango**piri**piri**sardinhas**grelhadas**grilled**bacalhau**salted**cod**fish**arroz**tamboril**monkfish**rice**cozida**portuguesa**pork**cabbage**stew**feijoada**bean**caldeirada**camarao**prawns**pastel**nata**custard**pastry**doce**fino**do**algarve**marzipan**portuguese*");
		// categoryFeaturesMap
		// .put("Irish",
		// "*dublin**coddle**slow**cooker**corned**beef**cabbage**irish**soda**bread**colcannon**coffee**steak**guinness**pie**green**beer**lamb**stew**shepherd**champ**crubeens*");
		// categoryFeaturesMap
		// .put("British",
		// "*beef**wellington**scones**pies**mince**eccles**cakes**kedgeree**scotch**eggs**toad**hole**welsh**rarebit**roast**steak**ale**eton**mess**welsh**cawl**shepherd**yorkshire**puddings**sausage**rolls**rumbledethumps**cornish**pasties**jam**roly**poly**fish**chips**english**british*");
		// categoryFeaturesMap
		// .put("Greek",
		// "*greek**feta**moussaka**tiropites**baklava**horta**vrasta**avgolemono**tzatziki**pastitsio**galaktoboureko**fassolatha**spanakopita**youvetsi**dolmathakia**taramosalata**fassolakia**lathera**melomakarona**pork**souvlaki**domates**yemistes**tsoureki**keftethes**kourabiethes*");
		// categoryFeaturesMap
		// .put("Jewish",
		// "*challah**lox**gefilte**matzah**knishes**blintzes**cholent**jewish*");
		// categoryFeaturesMap
		// .put("Moroccan",
		// "*b’ssara**tagine**fish**chermoula**harira**kefta**couscous**makouda**zaalouk**b’stilla**moroccan*");
		// categoryFeaturesMap
		// .put("Himalayan/Nepalese",
		// "*daal**bhat**tarkari**chatamari**dheedo**aloo**tama**himalayan**nepalese*");
		// categoryFeaturesMap.put("Taiwanese",
		// "*taiwanese**meatballs**chitterlings**scallion*");
		// categoryFeaturesMap
		// .put("German",
		// "*apfelstrudel**eintopf**kasespatzle**kartoffelpuffer**rote**grutze**sauerbraten**brezel**schwarzwalder**kirschtorte**schnitzel**wurst**bratwurst**german*");
		// categoryFeaturesMap
		// .put("Australian",
		// "*crab**sticks**barramundi**vegemite**spag**bol**chiko**roll**australian*");
		// categoryFeaturesMap
		// .put("Scandinavian",
		// "*smorrebrod**cloudberry**danish**hash**gravlax**scandinavian**lefse*");
		// categoryFeaturesMap
		// .put("Filipino",
		// "*lumpia**sinigang**chicken**afritada**cassava**cake**pancit**palabok**ube**pork**adobo**bistek**chicharon**calamansi**whiskey**sour**bibingka**sizzling**sisig**kare**halo**lechon**biko**kaldereta**arroz**caldo**ukoy**tocino**filipino*");
		// categoryFeaturesMap
		// .put("Education",
		// "*pre**schools**swimming**cooking**nurseries**highschool**middleschool**gardening**barre**musical**instruments**camps**library**language**summer**camps**recording**rehearsal**studio**driving**school**tutor**vocational**technical**tutor**art**class**first**aid**class**college**university**dance**elementary**education*");
		// categoryFeaturesMap
		// .put("Beauty/Salons",
		// "*hair**extensions**lash**removal**waxing**tattoo**spray**tanning**hair**salon**piercing**cosmetics**laser**nail**stylist**beauty*");
		// categoryFeaturesMap
		// .put("Entertainment",
		// "*tour**horseback**stadium**arenas**opera**ballet**resorts**golf**boxing**dj**bars**boating**hiking**gastro**pubs**clubs**skatingrinks**arts**venues**paintball**mountain**biking**golf**balloon**paddle**rock**climbing**pool**halls**magicians**fencing**tennis**comedy**nightlife**gokart**jazz**lasertag**lounge**ski**resort**surf**soccer**karaoke**casino**bowling**skydiving**scuba**horse**racing**archery**arcade**rafting**kayaking**hookah*");
		// categoryFeaturesMap
		// .put("Grocery",
		// "*supermarket**grocery**butcher**drug**farmers**baker**icecream**yogurt**meat**cheese**seafood**market*");
		// categoryFeaturesMap
		// .put("Stores",
		// "*fabric**costume**discount**formal**wear**outdoor**gear**glass**mirrors**firewood**books**mag**video**music**leather**goods**toy**uniform**shoe**aquarium**kitchen**bath**outlet**lingerie**mattress**electronics**men's**accessories**cards**stationery**vinyl**records**knitting**supplies**wholesale**bespoke**thrift**gift**vape**brasseries**candy**clothing**hardware**sports**antiques**furniture**store*");
		// categoryFeaturesMap.put("Real estate",
		// "*estate**agents**mortgage**broker*");
		// categoryFeaturesMap.put("Event planning",
		// "*wedding**planning**event**party**photography*");
		// categoryFeaturesMap
		// .put("Installation and Repair",
		// "*windshield**appliances**drywall**watch**windows**insulation**sewing**alteration**solar**computer**garage**carpet*");
		// categoryFeaturesMap
		// .put("Home Decor",
		// "*home**decor**cleaning**flooring**chimney**sweep**tinting**roofing*");
		// categoryFeaturesMap
		// .put("Auto",
		// "*auto**parts**supplies**customization**detailing**loan**tires**car**wash**share**dealers**automotive**towing**stereo**wheel**rim**automobile*");
		// categoryFeaturesMap.put("Contractor",
		// "*contractor**buliding**movers**masonry**concrete*");
		// categoryFeaturesMap
		// .put("Rental",
		// "*rental**motorcycle**taxis**photobooth**video**Game**truck**vacation**limos*");
		// categoryFeaturesMap.put("Bank", "*bank**credit**union**insurance*");
		// categoryFeaturesMap
		// .put("Recreational",
		// "*dog**trampoline**recreational**amusement**parks**skate**playground**leisure**non-profit**community**centers**gym**sport**trainer**stadiums**arenas*");
		// categoryFeaturesMap
		// .put("Law",
		// "*law**DUI**attorny**criminal**defense**lawyer**personal**injury**bankruptcy**estate**notary*");
		// categoryFeaturesMap.put("Hotel",
		// "*hotel**housing**retirement**home**guest**house*");
		// categoryFeaturesMap.put("Office",
		// "*equipment**assistants**professional**office*");
		// categoryFeaturesMap
		// .put("Counselling",
		// "*college**counselling**career**financial**advising**life**coach*");
		// categoryFeaturesMap
		// .put("Professional",
		// "*artist**interior**design**employment**agencies**landscape**architect**barber**painter**electrician**profession**accountant**groomer*");
		// categoryFeaturesMap
		// .put("Film",
		// "*video**film**production**cinema**videographer**director**studio*");
		// categoryFeaturesMap.put("Printing Service",
		// "*graphic**design**screen**printing**print*");
		// categoryFeaturesMap
		// .put("Services",
		// "*service**post**office**bail**bondsmen**police**nanny**community**non-profit**handyman**gardener**public**government**funeral**cemeteries**matchmaker**plumbing**dry**cleaning** laundry**registration**property**management**museum**shipping**courier**delivery**airport**airlines**shuttle**caterer**catering**tranportation**travel**gas**oil**change**station*");
		// categoryFeaturesMap
		// .put("Massage",
		// "*massage**day**spas**relax**calm**clean**yoga**oils**acupuncture**physical**therapy**chiropractor**reiki*");
		// categoryFeaturesMap
		// .put("Yoga",
		// "*peace**quiet**relax**calm**yoga**stress**meditation**center**hot**instructors**studio**practice**class**massage**vibe**peaceful**meditate**pilates*");
		// categoryFeaturesMap
		// .put("Fitness",
		// "*diet**goals**staff**gym**work**out**results**exercise**mirrors**machines**lost**cardio**weight**workout**equipments**motivation**positive**loss**trainers**tanning**coach**membership**fitness*");
		// categoryFeaturesMap
		// .put("Spa",
		// "*spa**dealer**pools**landscape**chemical**pedicures**massage**chairs**technicians**ankle**mini**retreat**hydrotherapy**facial**skin**care**hair**manicures**brows*");
		// categoryFeaturesMap
		// .put("Physical Therapy",
		// "*encouraging**atmosphere**upbeat**manual**physical**therapy**therapeutic**exercise**work**conditioning**hardening**functional**capacity**evaluations**vestibular**sports**medicine*");
		// categoryFeaturesMap
		// .put("Reflexology",
		// "*anxiety**asthma**cancer**treatment**cardiovascular**issues**diabetes**headaches**kidney**function**PMS**sinusitis**cure**peace**pressure**acupuncture**acupressure**massage*");
		// categoryFeaturesMap
		// .put("Hearing Aid Providers",
		// "*hearing**loss**impairment**hearing aid**ear**deaf**plastic**case**microphone**amplifier**speaker**vibration**physician*");
		// categoryFeaturesMap
		// .put("Cannabis Clinics",
		// "*psychoactive**euphoria**relaxation**psychological**dependence**primary**caregiver**cannbis*");
		// categoryFeaturesMap
		// .put("Naturopathic/Holistic",
		// "*naturopathic**holistic**mental**factors**social**factors**healing**power**nature**underlying causes**self-healing**self**responsibilty**counseling**hygiene**homeopathy**acupuncture**intravenous**injection**therapy**naturopathic**obstetrics**holistic**proactive*");
		// categoryFeaturesMap
		// .put("Neurologist",
		// "*neurologist**brain**spinal**cord**nervous**peripheral**system**headaches**epilepsy**stroke**tremor**parkinson**disease**chronic**pain**dizziness**numbness**tingling**movement**problem**seizures**nerves**memory**stiff**neck*");
		// categoryFeaturesMap
		// .put("Diagnostic Service",
		// "*diagnose**prescribe**monitor**patient**results**laboratory**testing**imaging**services**adhd**audiology**laboratory**bone**mineral**dentistometry**echocardiography**ultrasound**holter**monitoring**event**monitoring**sleep**study**diagnostic**services**Radiology**teraradiology**nurse*");
		// categoryFeaturesMap
		// .put("Cosmetic Dentist",
		// "*appearance**gums**orthodontics**prosthodontics**bonding**porcelain**veneers**laminates**caps**crowns**enameloplasty**gingivectomy**whitening**bleaching**gum**depigmentation**pontics**false**teeth**ultra-thin**sculpts**gold**amalgam**porcelain**bridges**resin**zirconia**oral**self**esteem**invisible**braces**implants**reshape**straighten**misaligned**lemgthen**sedation**contouring**stained**crooked**shape*");
		// categoryFeaturesMap
		// .put("Veterinarians",
		// "*nonhumans**companion**livestock**zoo**cats**docking**tails**debarking**dogs**crop**diagnose**treatment**after**care**clinical**signs**exotic**animal**conservation**laboratory**animals**equine**reptiles**husbandry**meat**milk**egg**poultry**flocks**ovine**sheep**bovine**cattle**porcine**swine**food**foodborne**zooologists**aquatic**fish**farms**pet**horses**veterinarians**vaccinations**wild**creatures**sharks**bite**kick**scratch**dirty*");
		// categoryFeaturesMap
		// .put("Rehabilitation Center",
		// "*inpatient**hospitals**less**costly**facilities**speech**occupational**therapy**mursing**care**lifetime**days**physical**medicine**rehab*");
		// categoryFeaturesMap
		// .put("Dentistry",
		// "*cavity**mucosa**odontology**tooth**size**structure**abnormalities**dentist**root**molar**abscessed**drill**cavity**decay**periodontal**pyorrhea**restoration**extraction**removal**implants**prosthetic**endodontic**checkup**epidemology**dentures**bridges**smiles**systemic**disease**decay**teeth**gum**oral**cleaning**maintainence**filling**extraction**polishing**floss**brush**developing**plaque**flouride**strong**healthy**X-ray**silver**filling*");
		// categoryFeaturesMap
		// .put("Life Coach",
		// "*counsels**encourage**careers**personal**challenges**examining**coach**client**growth*");
		// categoryFeaturesMap
		// .put("Psychologists",
		// "*behavior**studies**mental**counseling**social**consultation**psychotherapy**issues**depressed**angry**anxious**addictions**psychologist*");
		// categoryFeaturesMap
		// .put("Home Health Care",
		// "*home**illness**injury**convenient**effective**wound**care**nurtition**therapy**injections**monitoring**health**independecne**self**sufficient**needs**questions**blood**pressure**temperature**breathing**heartrate**pain**safety*");
		// categoryFeaturesMap
		// .put("Midwives",
		// "*care**mothers**infants**baby**skill**expertise**pregnancy**birth**optimal**recovery**maternity**trusting**women**primary**health**labor**postpartum**contraceptive**family**planning**medication**responsible**accountable**home**vaginal**reproduction**childbearing**cycle**midwives*");
		// categoryFeaturesMap
		// .put("Pediatricians",
		// "*care**infants**children**child**adolescents**congenital**defects**genetic**variance**development**issues**minors**decisions**guardianship**privacy**informed**family**emptional**medical**pediatric**nurse**acute**infectious**malignancies**development**functional**depression**critical**newborn**immunizations**behavior**fitness*");
		// categoryFeaturesMap
		// .put("Fertility Clinics",
		// "*clinics**fertility**infertility**miscarriages**semen**analysis**reprodcutive**technology**child**birth**clinic**quality**couples**assist**parents**unachieve**conception**pregnancy*");
		// categoryFeaturesMap
		// .put("Counselling and Mental Health",
		// "*absence**mental**health**well**being**self**worth**stress**dilema**decision**day**genetic**traumatic**physical**confused**anxious**scared**emotional**friend**family**counsellor**confiding**empathise**physical**sport**eat**healthy**balaned**break**neurotic**anxiety**depression**phobia**panic**schizophrenia**bipolar**personality**disorder**loss**dirty**untidy**reckless**psychological**social**nutrition**risk**perception**grief**service*");
		// categoryFeaturesMap
		// .put("Urologists",
		// "*urologists**male**female**urinary**tract**system**enlarge**prostate**cancers**testicular**infertility**kidney**stones**sexual**erectile**dysfunction*");
		// categoryFeaturesMap
		// .put("Allergists",
		// "*allergists**hypersensitive**immune**response**allergen**allergy**allergies**rash**antibodies**sensitization**symptoms**blocked**itchy**runny**watery**cough**peeling**swelling**bleeding**anaphylaxis**hives**anxiety**swollen**lips**asthma*");
		// categoryFeaturesMap
		// .put("Cardiologists",
		// "*cardiologists**heart**attack**stroke**cardiac**arrest**chest**pressure**heartbeat**weight**gain**lifestyle**ecg**dizzy**history**blood**vessels**echocardiogram**catheterization**angiogram**coronary*");
		// categoryFeaturesMap
		// .put("Periodontists",
		// "*periodontal**disease**dental**implants**oral**inflamation**cosmetic**gums**teeth**gingiva**alveolar**cementum**plaque**gingivitis**peri**implantis**scaling**root**planning**tartar**bleeding**swelling**mouthrinse**chlorhexidine**doxycycline**minocycline**antimicrobial**mouthwash**flap**brush**floss*");
		// categoryFeaturesMap
		// .put("Audiologist",
		// "*auditory**vestibular**hearing**balance**aid**loss**assistive**listening**device**cochlear**implant**communicate**speech**pathology*");
		// categoryFeaturesMap
		// .put("Podiatrists",
		// "*foot**ankle**leg**podiatric**medicine**physician**lower**extremity**anatomy**physiology*");
		// categoryFeaturesMap
		// .put("Ear, Nose, Throat Specialist",
		// "*otolarynology**otolaryngologists**ear**nose**throat**sinus**hearing**nasal**larynx**oral**pharynx**facial**cranial**paranasal**smell**respiration**aerodigestive**esophagus**infectious**deformities**reconstructive**inhalant*");
		// categoryFeaturesMap
		// .put("Endocrinologists",
		// "*glands**hormonal**menopause**diabetes**metabolic**osteoporosis**thyroid**endocrine**cholesterol**hypertension**infertility*");
		// categoryFeaturesMap
		// .put("Chiropractors",
		// "*chiropractor**neuromuscular**disorder**spine**exercise**ergonomy**biomechanical**structural**dearrangement**restore**neurological**pressure**chiropractic**spinal**nerve**lower**back**leg**neck**arthritic**injury**musculoskeletal**nervous**drug**free**handson**nutritional**dietary**joints**tissues**system**massage**sublaxation**biomechanics**table**adjustment**dislocate**manipulation**posture**self**care**stress**trigger**point*");
		// categoryFeaturesMap
		// .put("Health & Medical",
		// "*family**surgeons**medical**centers**walk-in**clinic**appointment**doctors**waiting**emergency**diagnostic**imaging**xrays**ct**scans**mri**hospitals**internal**medicine*");
		// categoryFeaturesMap
		// .put("Obstetricians & Gynecologists",
		// "*pregnancy**labor**peuperium**obstetrician**gynecologist**female**reproductive**reproduction**prenatal**pap**screening**family**planning**adolescent**endocrinology**infertile**infertility**delivery**urinary**tract**sonography**ultrasonography**ultrasound**maternal**fetal**newborn**baby**pelvic**childbirth**menopause**postpartum**ultrasound**weeks**blood**rubella**urinalysis**count**trimester**midwife**first**second**third**glucose**gestational**screening*");
		// categoryFeaturesMap
		// .put("Cosmetic Surgeon",
		// "*elective**liposuction**breast**augmentation**reduction**abdominoplasty**botox**hyaluronic**acid**laser**hair**removal**skin**microdermabrasion**tummy**tuck**plastic**boob**job**eyelid**silicon**gel**prosthetics**mastectomy**grafting**saline**lift**buttock**chemical**peel**wrinkled**nose**ears**ear**facelift**brow**chin**cheek**collagen*");
		// categoryFeaturesMap
		// .put("Dermatologists",
		// "*skin**hair**nails**nail**mucous**membrane**eczema**acne**psorasis**infection**wrinkles**aging**acne**dermatology**scars**birthmark**neonatal**skin**fat**oral**membrane**dermatohistopathology**cosmetic**laser**radiotherapy**photodynamic**warts**dermatitis**atopic*");
		// categoryFeaturesMap
		// .put("Psychiatrists",
		// "*psychological**mental**emotional**behavioral**psychotherapy**stress**crisis**addiction**forensic**geriatric**psychosomatic**sleep**neurophysiology**crosscultural**neurodevelopmental**anxiety**schizophrenia**consultation*");
		// categoryFeaturesMap
		// .put("Radiologists",
		// "*medical**imaging**mri**cancer**symptom**radiology**diagnostic**interventional**oncology*");
		// categoryFeaturesMap
		// .put("Oncologists",
		// "*cancer**chemotherapy**targeted**tumor**oncologist**biopsy**cells**uterine**cervical**leukemia**osteosarcoma**ewings**sarcoma**lymphomas**myelomas**biopsies**endoscopy**ultrasound**malignant**mammography**neurooncology**hematooncology**urooncology*");
		// categoryFeaturesMap
		// .put("Speech Therapists",
		// "*speech**voice**language**sound**stutter**fluency**rhythm**pitch**harsh**counselling**articulation**resonance**dysphagia**receptive**expressive**phonation**syntax**semantics**cognitive**swallowing*");
		// categoryFeaturesMap
		// .put("Ophthalmologists",
		// "*eye**visual**vision**lenses**spectacles**ocular**manifestation**distorted**vision**floaters**flashes**haloes**peripheral**tearing**eyelid**cornea**pupil**intraocular**iris**drops**glaucomia**vitreoretinal*");

		return categoryFeaturesMap;
	}

}
