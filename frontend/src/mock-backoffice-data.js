import tours from './mock-tours.js';

export { tours };

export const initialBookings = [
  {
    id: 'BK-101',
    tourTitle: 'Plitvice Lakes Guided Hiking Tour',
    userName: 'Ana Horvat',
    userEmail: 'ana.horvat@example.com',
    participantCount: 4,
    selectedAddons: ['Transport from Zagreb', 'Packed lunch'],
    status: 'Confirmed',
    createdAt: '2026-10-18',
  },
  {
    id: 'BK-102',
    tourTitle: 'Zagreb Historic Centre Walking Tour',
    userName: 'Marko Kovač',
    userEmail: 'marko.kovac@example.com',
    participantCount: 2,
    selectedAddons: ['Audio guide'],
    status: 'Confirmed',
    createdAt: '2026-10-19',
  },
  {
    id: 'BK-103',
    tourTitle: 'Cetina River Rafting Adventure',
    userName: 'Luka Novak',
    userEmail: 'luka.novak@example.com',
    participantCount: 6,
    selectedAddons: ['Wetsuit rental', 'Transport from Omiš'],
    status: 'Confirmed',
    createdAt: '2026-10-19',
  },
  {
    id: 'BK-104',
    tourTitle: 'Dubrovnik Old Town Cultural Walking Tour',
    userName: 'Elena Rossi',
    userEmail: 'elena.rossi@example.com',
    participantCount: 3,
    selectedAddons: ['City walls entry ticket', 'Audio guide'],
    status: 'Completed',
    createdAt: '2026-10-15',
  },
  {
    id: 'BK-105',
    tourTitle: 'Split Coastline and Islands Boat Excursion',
    userName: 'David Müller',
    userEmail: 'david.muller@example.com',
    participantCount: 2,
    selectedAddons: ['Snorkelling equipment rental', 'Lunch'],
    status: 'Cancelled',
    createdAt: '2026-10-16',
  },
  {
    id: 'BK-106',
    tourTitle: 'Medvednica and Sljeme Hiking Excursion',
    userName: 'Petra Babić',
    userEmail: 'petra.babic@example.com',
    participantCount: 5,
    selectedAddons: ['Hiking pole rental'],
    status: 'Confirmed',
    createdAt: '2026-10-20',
  },
];

export const initialGuides = [
  {
    id: 'G-1',
    name: 'Ivan Perić',
    phone: '+385 91 234 5678',
    assignedActivities: ['Plitvice Lakes Guided Hiking Tour (Nov 8, 08:30)'],
    status: 'Assigned',
  },
  {
    id: 'G-2',
    name: 'Maja Jurić',
    phone: '+385 98 765 4321',
    assignedActivities: ['Zagreb Historic Centre Walking Tour (Nov 7, 10:00)'],
    status: 'Assigned',
  },
  {
    id: 'G-3',
    name: 'Tomislav Horvat',
    phone: '+385 92 345 6789',
    assignedActivities: [],
    status: 'Available',
  },
  {
    id: 'G-4',
    name: 'Lucija Kovačić',
    phone: '+385 95 876 5432',
    assignedActivities: [],
    status: 'Available',
  },
  {
    id: 'G-5',
    name: 'Filip Radić',
    phone: '+385 99 123 4567',
    assignedActivities: ['Cetina River Rafting Adventure (May 2, 09:00)'],
    status: 'Assigned',
  },
];

export const initialScheduledExcursions = [
  {
    id: 'EX-1',
    tourTitle: 'Zagreb Historic Centre Walking Tour',
    dateTime: '2026-11-07 10:00',
    requiredGuides: 1,
    assignedGuides: ['Maja Jurić'],
  },
  {
    id: 'EX-2',
    tourTitle: 'Plitvice Lakes Guided Hiking Tour',
    dateTime: '2026-11-08 08:30',
    requiredGuides: 2,
    assignedGuides: ['Ivan Perić'],
  },
  {
    id: 'EX-3',
    tourTitle: 'Medvednica and Sljeme Hiking Excursion',
    dateTime: '2026-11-08 09:00',
    requiredGuides: 1,
    assignedGuides: [],
  },
  {
    id: 'EX-4',
    tourTitle: 'Zagreb Guided Cycling Tour',
    dateTime: '2026-11-07 14:00',
    requiredGuides: 1,
    assignedGuides: [],
  },
  {
    id: 'EX-5',
    tourTitle: 'Cetina River Rafting Adventure',
    dateTime: '2027-05-02 09:00',
    requiredGuides: 2,
    assignedGuides: ['Filip Radić'],
  },
];

export const periodMetrics = {
  week: {
    totalBookings: 32,
    generatedRevenue: 3450,
  },
  month: {
    totalBookings: 142,
    generatedRevenue: 14250,
  },
  year: {
    totalBookings: 1480,
    generatedRevenue: 149200,
  },
};

export const dashboardMetrics = {
  totalBookings: 142,
  occupancyRate: 84.5,
  generatedRevenue: 14250,
  popularActivity: 'Plitvice Lakes Guided Hiking Tour',
  popularAddons: 'Transport from Zagreb & Audio guide',
};

export const topFiveActivities = [
  { rank: 1, title: 'Plitvice Lakes Guided Hiking Tour', category: 'Hiking', bookingsCount: 38 },
  { rank: 2, title: 'Dubrovnik Old Town Cultural Walking Tour', category: 'City tour', bookingsCount: 35 },
  { rank: 3, title: 'Zagreb Historic Centre Walking Tour', category: 'City tour', bookingsCount: 32 },
  { rank: 4, title: 'Cetina River Rafting Adventure', category: 'Rafting', bookingsCount: 28 },
  { rank: 5, title: 'Split Coastline and Islands Boat Excursion', category: 'Boat trip', bookingsCount: 24 },
];

export const topFiveAddons = [
  { rank: 1, name: 'Audio guide', count: 86 },
  { rank: 2, name: 'Transport from Zagreb', count: 64 },
  { rank: 3, name: 'Packed lunch', count: 52 },
  { rank: 4, name: 'City walls entry ticket', count: 45 },
  { rank: 5, name: 'Snorkelling equipment rental', count: 38 },
];

export const tourPerformanceData = [
  {
    id: 1,
    title: 'Zagreb Historic Centre Walking Tour',
    dateTime: '2026-11-07 10:00',
    category: 'City tour',
    bookingsCount: 18,
    capacity: 18,
    occupancyRate: 100.0,
    revenue: 450,
  },
  {
    id: 2,
    title: 'Zagreb Historic Centre Walking Tour',
    dateTime: '2026-11-14 10:00',
    category: 'City tour',
    bookingsCount: 14,
    capacity: 18,
    occupancyRate: 77.8,
    revenue: 350,
  },
  {
    id: 3,
    title: 'Plitvice Lakes Guided Hiking Tour',
    dateTime: '2026-11-08 08:30',
    category: 'Hiking',
    bookingsCount: 12,
    capacity: 12,
    occupancyRate: 100.0,
    revenue: 780,
  },
  {
    id: 4,
    title: 'Plitvice Lakes Guided Hiking Tour',
    dateTime: '2026-11-15 08:30',
    category: 'Hiking',
    bookingsCount: 10,
    capacity: 12,
    occupancyRate: 83.3,
    revenue: 650,
  },
  {
    id: 5,
    title: 'Medvednica and Sljeme Hiking Excursion',
    dateTime: '2026-11-08 09:00',
    category: 'Hiking',
    bookingsCount: 9,
    capacity: 15,
    occupancyRate: 60.0,
    revenue: 288,
  },
  {
    id: 6,
    title: 'Medvednica and Sljeme Hiking Excursion',
    dateTime: '2026-11-15 09:00',
    category: 'Hiking',
    bookingsCount: 6,
    capacity: 15,
    occupancyRate: 40.0,
    revenue: 192,
  },
  {
    id: 7,
    title: 'Zagreb Guided Cycling Tour',
    dateTime: '2026-11-07 14:00',
    category: 'Cycling',
    bookingsCount: 14,
    capacity: 20,
    occupancyRate: 70.0,
    revenue: 686,
  },
  {
    id: 8,
    title: 'Cetina River Rafting Adventure',
    dateTime: '2027-05-02 09:00',
    category: 'Rafting',
    bookingsCount: 20,
    capacity: 24,
    occupancyRate: 83.3,
    revenue: 1540,
  },
  {
    id: 9,
    title: 'Split Coastline and Islands Boat Excursion',
    dateTime: '2026-11-09 09:30',
    category: 'Boat trip',
    bookingsCount: 18,
    capacity: 20,
    occupancyRate: 90.0,
    revenue: 1260,
  },
  {
    id: 10,
    title: 'Dubrovnik Old Town Cultural Walking Tour',
    dateTime: '2026-11-10 10:00',
    category: 'City tour',
    bookingsCount: 35,
    capacity: 40,
    occupancyRate: 87.5,
    revenue: 1400,
  },
];

export const initialEmployees = [
  {
    id: 'EMP-1',
    name: 'Ivan Perić',
    email: 'ivan.peric@example.com',
    role: 'Guide',
  },
  {
    id: 'EMP-2',
    name: 'Maja Jurić',
    email: 'maja.juric@example.com',
    role: 'Guide',
  },
  {
    id: 'EMP-3',
    name: 'Tomislav Horvat',
    email: 'tomislav.horvat@example.com',
    role: 'Organizer',
  },
  {
    id: 'EMP-4',
    name: 'Petra Babić',
    email: 'petra.babic@example.com',
    role: 'Administrator',
  },
  {
    id: 'EMP-5',
    name: 'Filip Radić',
    email: 'filip.radic@example.com',
    role: 'Guide',
  },
];

export const registeredUsersDirectory = [
  {
    id: 'USR-101',
    name: 'Ana Horvat',
    email: 'ana.horvat@example.com',
    currentRole: 'Client',
  },
  {
    id: 'USR-102',
    name: 'Marko Kovač',
    email: 'marko.kovac@example.com',
    currentRole: 'Client',
  },
  {
    id: 'USR-103',
    name: 'Luka Novak',
    email: 'luka.novak@example.com',
    currentRole: 'Client',
  },
  {
    id: 'USR-104',
    name: 'Elena Rossi',
    email: 'elena.rossi@example.com',
    currentRole: 'Client',
  },
  {
    id: 'USR-105',
    name: 'David Müller',
    email: 'david.muller@example.com',
    currentRole: 'Client',
  },
];
